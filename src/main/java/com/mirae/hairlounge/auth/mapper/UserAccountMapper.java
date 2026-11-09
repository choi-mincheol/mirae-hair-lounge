package com.mirae.hairlounge.auth.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.mirae.hairlounge.auth.domain.UserAccount;
import com.mirae.hairlounge.common.security.Role;

/**
 * user_account 테이블은 Auth만 씁니다(ADR-003).
 */
@Mapper
public interface UserAccountMapper {

    Optional<UserAccount> findByLoginId(String loginId);

    Optional<UserAccount> findById(long id);

    int countByRole(Role role);

    void insert(UserAccount account);
}
