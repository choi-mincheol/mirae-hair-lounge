package com.mirae.hairlounge.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.mirae.hairlounge.auth.mapper.UserAccountMapper;
import com.mirae.hairlounge.common.security.AccountStatus;
import com.mirae.hairlounge.common.security.UserAccountLookup;

/**
 * Common의 UserAccountLookup 포트 구현입니다. 순환 주입을 막기 위해 사용자 매퍼만 주입합니다(R-01, ADR-002).
 */
@Component
public class UserAccountLookupAdapter implements UserAccountLookup {

    private final UserAccountMapper userAccountMapper;

    public UserAccountLookupAdapter(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public Optional<AccountStatus> findById(long userId) {
        return userAccountMapper.findById(userId)
                .map(account -> new AccountStatus(account.getId(), account.getLoginId(), account.getName(),
                        account.getRole(), account.isEnabled()));
    }
}
