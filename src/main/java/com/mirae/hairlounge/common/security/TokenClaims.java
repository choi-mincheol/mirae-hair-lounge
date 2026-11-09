package com.mirae.hairlounge.common.security;

import java.time.Instant;

/**
 * 검증을 통과한 토큰 내용입니다. 토큰에는 사용자 id와 역할만 넣습니다(ADR-006).
 */
public record TokenClaims(long userId, Role role, Instant issuedAt, Instant expiresAt) {
}
