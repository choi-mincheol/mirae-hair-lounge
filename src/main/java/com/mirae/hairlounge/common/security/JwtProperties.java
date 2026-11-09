package com.mirae.hairlounge.common.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정입니다. 서명 키는 환경변수 MIRAE_JWT_SECRET으로만 받습니다(NFR3).
 *
 * @param secret       서명 키(32바이트 이상)
 * @param expiry       토큰 만료(마지막 발급 후 8시간, ADR-006)
 * @param reissueAfter 이 시간이 지난 토큰은 요청 때 새로 발급합니다(5분, ADR-006)
 */
@ConfigurationProperties("mirae.jwt")
public record JwtProperties(String secret, Duration expiry, Duration reissueAfter) {

    /** 서명 키가 로그에 남지 않게 합니다. */
    @Override
    public String toString() {
        return "JwtProperties{expiry=" + expiry + ", reissueAfter=" + reissueAfter + "}";
    }
}
