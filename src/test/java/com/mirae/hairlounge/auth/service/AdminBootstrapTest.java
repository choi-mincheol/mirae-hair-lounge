package com.mirae.hairlounge.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.SpringApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mirae.hairlounge.MiraeHairLoungeApplication;
import com.mirae.hairlounge.TestClockConfig;
import com.mirae.hairlounge.auth.domain.UserAccount;
import com.mirae.hairlounge.auth.mapper.UserAccountMapper;
import com.mirae.hairlounge.common.security.Role;

class AdminBootstrapTest {

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final Clock clock = Clock.fixed(TestClockConfig.FIXED_INSTANT, TestClockConfig.SEOUL);
    private UserAccountMapper userAccountMapper;

    @BeforeEach
    void setUp() {
        userAccountMapper = mock(UserAccountMapper.class);
    }

    @Test
    void createsAdminFromEnvironmentWhenNoAdminExists() {
        when(userAccountMapper.countByRole(Role.ADMIN)).thenReturn(0);

        bootstrap(new AdminProperties("owner", "owner-password-1", "관리자")).ensureAdminExists();

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountMapper).insert(captor.capture());
        UserAccount created = captor.getValue();
        assertThat(created.getLoginId()).isEqualTo("owner");
        assertThat(created.getName()).isEqualTo("관리자");
        assertThat(created.getRole()).isEqualTo(Role.ADMIN);
        assertThat(created.isEnabled()).isTrue();
        assertThat(created.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 11, 9, 10, 0));
        assertThat(created.getPasswordHash()).isNotEqualTo("owner-password-1");
        assertThat(passwordEncoder.matches("owner-password-1", created.getPasswordHash())).isTrue();
    }

    @Test
    void failsWithVariableNamesWhenNoAdminAndEnvironmentMissing() {
        when(userAccountMapper.countByRole(Role.ADMIN)).thenReturn(0);
        AdminBootstrap bootstrap = bootstrap(new AdminProperties("", null, "관리자"));

        assertThatThrownBy(bootstrap::ensureAdminExists)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MIRAE_ADMIN_LOGIN_ID")
                .hasMessageContaining("MIRAE_ADMIN_PASSWORD");
        verify(userAccountMapper, never()).insert(any());
    }

    @Test
    void namesOnlyTheMissingVariable() {
        when(userAccountMapper.countByRole(Role.ADMIN)).thenReturn(0);
        AdminBootstrap bootstrap = bootstrap(new AdminProperties("owner", " ", "관리자"));

        assertThatThrownBy(bootstrap::ensureAdminExists)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MIRAE_ADMIN_PASSWORD")
                .hasMessageNotContaining("MIRAE_ADMIN_LOGIN_ID");
    }

    @Test
    void doesNothingWhenAdminAlreadyExistsEvenWithoutEnvironment() {
        when(userAccountMapper.countByRole(Role.ADMIN)).thenReturn(1);

        bootstrap(new AdminProperties(null, null, "관리자")).ensureAdminExists();

        verify(userAccountMapper, never()).insert(any());
    }

    @Test
    void applicationStartupFailsOnEmptyDatabaseWithoutAdminEnvironment() {
        assertThatThrownBy(() -> SpringApplication.run(MiraeHairLoungeApplication.class,
                "--spring.profiles.active=test",
                "--server.port=0",
                "--spring.datasource.url=jdbc:h2:mem:admin-bootstrap-missing-env",
                "--mirae.admin.login-id=",
                "--mirae.admin.password=").close())
                .hasStackTraceContaining("MIRAE_ADMIN_LOGIN_ID");
    }

    private AdminBootstrap bootstrap(AdminProperties properties) {
        return new AdminBootstrap(userAccountMapper, passwordEncoder, properties, clock);
    }
}
