package com.mirae.hairlounge.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 발급·검증·재발급 판단(ADR-005, ADR-006). 시간은 주입한 시계로만 계산합니다(NFR8).
 */
@Service
public class JwtTokenService {

    static final String SECRET_ENV = "MIRAE_JWT_SECRET";
    private static final int MIN_SECRET_BYTES = 32;
    private static final String ROLE_CLAIM = "role";

    private final SecretKey key;
    private final Duration expiry;
    private final Duration reissueAfter;
    private final Clock clock;
    private final JwtParser parser;

    public JwtTokenService(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(validSecretBytes(properties.secret()));
        this.expiry = properties.expiry();
        this.reissueAfter = properties.reissueAfter();
        this.clock = clock;
        this.parser = Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build();
    }

    public String issue(long userId, Role role) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiry)))
                .signWith(key)
                .compact();
    }

    public TokenClaims parse(String token) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return new TokenClaims(
                    Long.parseLong(claims.getSubject()),
                    Role.valueOf(claims.get(ROLE_CLAIM, String.class)),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant());
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(true, e);
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            throw new InvalidTokenException(false, e);
        }
    }

    /** 발급된 지 5분이 지났으면 새 토큰으로 바꿉니다. 그래서 만료는 마지막 활동 후 8시간입니다(ADR-006). */
    public boolean shouldReissue(TokenClaims claims) {
        return !clock.instant().isBefore(claims.issuedAt().plus(reissueAfter));
    }

    public Duration expiry() {
        return expiry;
    }

    private static byte[] validSecretBytes(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT 서명 키가 없어 시작할 수 없습니다. 환경변수 " + SECRET_ENV + "를 설정한 뒤 다시 시작하세요.");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "환경변수 " + SECRET_ENV + "의 값은 " + MIN_SECRET_BYTES + "바이트 이상이어야 합니다.");
        }
        return bytes;
    }
}
