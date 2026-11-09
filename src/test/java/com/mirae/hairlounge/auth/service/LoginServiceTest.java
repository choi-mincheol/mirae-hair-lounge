package com.mirae.hairlounge.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mirae.hairlounge.auth.domain.UserAccount;
import com.mirae.hairlounge.auth.mapper.UserAccountMapper;
import com.mirae.hairlounge.common.security.LoginUser;
import com.mirae.hairlounge.common.security.Role;

class LoginServiceTest {

    private static final String FAILURE_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다";
    private static final String RAW_PASSWORD = "password123!";

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private UserAccountMapper userAccountMapper;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        userAccountMapper = mock(UserAccountMapper.class);
        loginService = new LoginService(userAccountMapper, passwordEncoder);
    }

    @Test
    void logsInWithCorrectPassword() {
        when(userAccountMapper.findByLoginId("staff01")).thenReturn(Optional.of(account("staff01", true)));

        LoginUser user = loginService.login("staff01", RAW_PASSWORD);

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getLoginId()).isEqualTo("staff01");
        assertThat(user.getDisplayName()).isEqualTo("김직원");
        assertThat(user.getRole()).isEqualTo(Role.STAFF);
    }

    @Test
    void rejectsWrongPassword() {
        when(userAccountMapper.findByLoginId("staff01")).thenReturn(Optional.of(account("staff01", true)));

        assertThatThrownBy(() -> loginService.login("staff01", "wrong-password"))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage(FAILURE_MESSAGE);
    }

    @Test
    void rejectsDisabledAccountWithSameMessageEvenWithCorrectPassword() {
        when(userAccountMapper.findByLoginId("staff02")).thenReturn(Optional.of(account("staff02", false)));

        assertThatThrownBy(() -> loginService.login("staff02", RAW_PASSWORD))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage(FAILURE_MESSAGE);
    }

    @Test
    void rejectsUnknownLoginIdWithSameMessage() {
        when(userAccountMapper.findByLoginId("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login("nobody", RAW_PASSWORD))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage(FAILURE_MESSAGE);
    }

    @Test
    void rejectsBlankInputWithoutLookingUp() {
        assertThatThrownBy(() -> loginService.login("  ", RAW_PASSWORD))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage(FAILURE_MESSAGE);
        assertThatThrownBy(() -> loginService.login("staff01", null))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage(FAILURE_MESSAGE);
    }

    private UserAccount account(String loginId, boolean enabled) {
        UserAccount account = new UserAccount();
        account.setId(7L);
        account.setLoginId(loginId);
        account.setName("김직원");
        account.setPasswordHash(passwordEncoder.encode(RAW_PASSWORD));
        account.setRole(Role.STAFF);
        account.setEnabled(enabled);
        account.setCreatedAt(LocalDateTime.of(2026, 11, 1, 9, 0));
        return account;
    }
}
