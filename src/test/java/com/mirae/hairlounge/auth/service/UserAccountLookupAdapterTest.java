package com.mirae.hairlounge.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mirae.hairlounge.auth.domain.UserAccount;
import com.mirae.hairlounge.auth.mapper.UserAccountMapper;
import com.mirae.hairlounge.common.security.AccountStatus;
import com.mirae.hairlounge.common.security.Role;

class UserAccountLookupAdapterTest {

    private final UserAccountMapper userAccountMapper = mock(UserAccountMapper.class);
    private final UserAccountLookupAdapter adapter = new UserAccountLookupAdapter(userAccountMapper);

    @Test
    void returnsStatusAndRoleWithoutPasswordHash() {
        when(userAccountMapper.findById(5L)).thenReturn(Optional.of(account(5L, true)));

        AccountStatus status = adapter.findById(5L).orElseThrow();

        assertThat(status.id()).isEqualTo(5L);
        assertThat(status.loginId()).isEqualTo("staff01");
        assertThat(status.name()).isEqualTo("김직원");
        assertThat(status.role()).isEqualTo(Role.STAFF);
        assertThat(status.enabled()).isTrue();
        assertThat(status.toString()).doesNotContain("secret-hash");
    }

    @Test
    void reportsDisabledAccount() {
        when(userAccountMapper.findById(6L)).thenReturn(Optional.of(account(6L, false)));

        assertThat(adapter.findById(6L)).get().extracting(AccountStatus::enabled).isEqualTo(false);
    }

    @Test
    void returnsEmptyForUnknownAccount() {
        when(userAccountMapper.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    private static UserAccount account(long id, boolean enabled) {
        UserAccount account = new UserAccount();
        account.setId(id);
        account.setLoginId("staff01");
        account.setName("김직원");
        account.setPasswordHash("secret-hash");
        account.setRole(Role.STAFF);
        account.setEnabled(enabled);
        return account;
    }
}
