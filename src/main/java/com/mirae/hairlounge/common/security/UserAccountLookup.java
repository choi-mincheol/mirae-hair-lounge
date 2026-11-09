package com.mirae.hairlounge.common.security;

import java.util.Optional;

/**
 * JWT 필터가 계정 상태를 확인하는 포트입니다. Common이 정의하고 Auth가 구현합니다(ADR-002, R-01).
 */
public interface UserAccountLookup {

    Optional<AccountStatus> findById(long userId);
}
