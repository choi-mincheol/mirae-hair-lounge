package com.mirae.hairlounge.auth.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.mirae.hairlounge.auth.domain.UserAccount;
import com.mirae.hairlounge.common.security.Role;

@MybatisTest
@ActiveProfiles("test")
@Sql("/sql/skeleton-users.sql")
class UserAccountMapperTest {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Test
    void findsAccountByLoginId() {
        Optional<UserAccount> found = userAccountMapper.findByLoginId("admin01");

        assertThat(found).isPresent();
        UserAccount account = found.get();
        assertThat(account.getId()).isEqualTo(1001L);
        assertThat(account.getName()).isEqualTo("홍길동");
        assertThat(account.getRole()).isEqualTo(Role.ADMIN);
        assertThat(account.isEnabled()).isTrue();
        assertThat(account.getPasswordHash()).isEqualTo("not-a-real-hash");
        assertThat(account.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 11, 1, 9, 0));
    }

    @Test
    void returnsEmptyForUnknownLoginId() {
        assertThat(userAccountMapper.findByLoginId("nobody")).isEmpty();
        assertThat(userAccountMapper.findById(9999L)).isEmpty();
    }

    @Test
    void returnsDisabledAccountWithEnabledFalse() {
        UserAccount disabled = userAccountMapper.findByLoginId("staff02").orElseThrow();

        assertThat(disabled.isEnabled()).isFalse();
        assertThat(disabled.getRole()).isEqualTo(Role.STAFF);
    }

    @Test
    void findsAccountById() {
        UserAccount staff = userAccountMapper.findById(1002L).orElseThrow();

        assertThat(staff.getLoginId()).isEqualTo("staff01");
    }

    @Test
    void countsAccountsByRole() {
        assertThat(userAccountMapper.countByRole(Role.ADMIN)).isEqualTo(1);
        assertThat(userAccountMapper.countByRole(Role.STAFF)).isEqualTo(2);
    }

    @Test
    void insertsAccountAndAssignsId() {
        UserAccount account = newAccount("newadmin");

        userAccountMapper.insert(account);

        assertThat(account.getId()).isNotNull();
        assertThat(userAccountMapper.findByLoginId("newadmin")).get()
                .extracting(UserAccount::getId).isEqualTo(account.getId());
    }

    @Test
    void rejectsDuplicateLoginIdOnInsert() {
        assertThatThrownBy(() -> userAccountMapper.insert(newAccount("admin01")))
                .isInstanceOf(DuplicateKeyException.class);
    }

    private static UserAccount newAccount(String loginId) {
        UserAccount account = new UserAccount();
        account.setLoginId(loginId);
        account.setName("새 관리자");
        account.setPasswordHash("not-a-real-hash");
        account.setRole(Role.ADMIN);
        account.setEnabled(true);
        account.setCreatedAt(LocalDateTime.of(2026, 11, 9, 10, 0));
        return account;
    }
}
