package com.mirae.hairlounge.common.security;

/**
 * 사용자 역할입니다. 권한 규칙은 이 두 역할로만 나눕니다(요구사항 2절).
 */
public enum Role {
    ADMIN("관리자"),
    STAFF("직원");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Spring Security 권한 이름(ROLE_ADMIN, ROLE_STAFF)입니다. */
    public String authority() {
        return "ROLE_" + name();
    }
}
