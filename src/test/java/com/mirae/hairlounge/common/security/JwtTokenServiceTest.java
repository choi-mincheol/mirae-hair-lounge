package com.mirae.hairlounge.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private static final String SECRET = "test-only-dummy-signing-key-not-a-real-secret-0123456789";
    private static final String OTHER_SECRET = "another-test-only-dummy-signing-key-not-a-secret-987654";
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant ISSUED_AT = Instant.parse("2026-11-09T01:00:00Z");

    @Test
    void issuedTokenParsesBackToUserAndRole() {
        JwtTokenService service = serviceAt(ISSUED_AT, SECRET);

        String token = service.issue(42L, Role.STAFF);
        TokenClaims claims = service.parse(token);

        assertThat(claims.userId()).isEqualTo(42L);
        assertThat(claims.role()).isEqualTo(Role.STAFF);
        assertThat(claims.issuedAt()).isEqualTo(ISSUED_AT);
        assertThat(claims.expiresAt()).isEqualTo(ISSUED_AT.plus(Duration.ofHours(8)));
    }

    @Test
    void tokenIsStillValidOneSecondBeforeEightHours() {
        String token = serviceAt(ISSUED_AT, SECRET).issue(1L, Role.ADMIN);

        TokenClaims claims = serviceAt(ISSUED_AT.plus(Duration.ofHours(8)).minusSeconds(1), SECRET).parse(token);

        assertThat(claims.userId()).isEqualTo(1L);
    }

    @Test
    void rejectsTokenAfterEightHoursAsExpired() {
        String token = serviceAt(ISSUED_AT, SECRET).issue(1L, Role.ADMIN);
        JwtTokenService later = serviceAt(ISSUED_AT.plus(Duration.ofHours(8)).plusSeconds(1), SECRET);

        assertThatThrownBy(() -> later.parse(token))
                .isInstanceOf(InvalidTokenException.class)
                .satisfies(e -> assertThat(((InvalidTokenException) e).isExpired()).isTrue());
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String forged = serviceAt(ISSUED_AT, OTHER_SECRET).issue(1L, Role.ADMIN);

        assertThatThrownBy(() -> serviceAt(ISSUED_AT, SECRET).parse(forged))
                .isInstanceOf(InvalidTokenException.class)
                .satisfies(e -> assertThat(((InvalidTokenException) e).isExpired()).isFalse());
    }

    @Test
    void rejectsMalformedOrEmptyToken() {
        JwtTokenService service = serviceAt(ISSUED_AT, SECRET);

        assertThatThrownBy(() -> service.parse("not-a-jwt")).isInstanceOf(InvalidTokenException.class);
        assertThatThrownBy(() -> service.parse("")).isInstanceOf(InvalidTokenException.class);
        assertThatThrownBy(() -> service.parse(null)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void reissuesOnlyAfterFiveMinutes() {
        String token = serviceAt(ISSUED_AT, SECRET).issue(1L, Role.ADMIN);

        JwtTokenService atFourMinutes59 = serviceAt(ISSUED_AT.plusSeconds(4 * 60 + 59), SECRET);
        JwtTokenService atFiveMinutes = serviceAt(ISSUED_AT.plusSeconds(5 * 60), SECRET);

        assertThat(atFourMinutes59.shouldReissue(atFourMinutes59.parse(token))).isFalse();
        assertThat(atFiveMinutes.shouldReissue(atFiveMinutes.parse(token))).isTrue();
    }

    @Test
    void refusesToStartWithoutSecretAndNamesEnvironmentVariable() {
        assertThatThrownBy(() -> serviceAt(ISSUED_AT, ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MIRAE_JWT_SECRET");
        assertThatThrownBy(() -> serviceAt(ISSUED_AT, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MIRAE_JWT_SECRET");
    }

    @Test
    void refusesSecretShorterThan32Bytes() {
        assertThatThrownBy(() -> serviceAt(ISSUED_AT, "0123456789abcdef0123456789abcde"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32바이트")
                .hasMessageNotContaining("0123456789abcdef");
    }

    private static JwtTokenService serviceAt(Instant now, String secret) {
        JwtProperties properties = new JwtProperties(secret, Duration.ofHours(8), Duration.ofMinutes(5));
        return new JwtTokenService(properties, Clock.fixed(now, SEOUL));
    }
}
