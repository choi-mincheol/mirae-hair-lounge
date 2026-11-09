package com.mirae.hairlounge.auth.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 첫 관리자 계정 값입니다. 환경변수 MIRAE_ADMIN_LOGIN_ID, MIRAE_ADMIN_PASSWORD로만 받습니다(FR1.2, NFR3).
 */
@ConfigurationProperties("mirae.admin")
public record AdminProperties(String loginId, String password, String name) {

    /** 비밀번호가 로그에 남지 않게 합니다. */
    @Override
    public String toString() {
        return "AdminProperties{loginId=" + loginId + ", name=" + name + "}";
    }
}
