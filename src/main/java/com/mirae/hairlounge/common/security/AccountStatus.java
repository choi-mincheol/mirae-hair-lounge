package com.mirae.hairlounge.common.security;

/**
 * 토큰 검사 때 필요한 계정 상태입니다. 비밀번호 해시는 담지 않습니다(ADR-002).
 */
public record AccountStatus(long id, String loginId, String name, Role role, boolean enabled) {
}
