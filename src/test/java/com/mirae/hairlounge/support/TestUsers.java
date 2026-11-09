package com.mirae.hairlounge.support;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mirae.hairlounge.common.security.JwtTokenService;
import com.mirae.hairlounge.common.security.Role;

import jakarta.servlet.http.Cookie;

import com.mirae.hairlounge.common.security.AuthCookies;

/**
 * 테스트 안에서 로그인 가능한 사용자를 만들고 JWT 쿠키를 만들어 주는 도우미입니다.
 * 비밀번호는 테스트 전용 값이며 PasswordEncoder로 해시해 넣습니다.
 */
@Component
public class TestUsers {

    public static final String PASSWORD = "test-password-1";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;

    public TestUsers(JdbcTemplate jdbc, PasswordEncoder passwordEncoder, JwtTokenService tokenService) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public long create(String loginId, String name, Role role, boolean enabled) {
        jdbc.update("INSERT INTO user_account (login_id, name, password_hash, role, enabled, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?)", loginId, name, passwordEncoder.encode(PASSWORD), role.name(), enabled,
                Timestamp.valueOf(LocalDateTime.of(2026, 11, 1, 9, 0)));
        return jdbc.queryForObject("SELECT id FROM user_account WHERE login_id = ?", Long.class, loginId);
    }

    public Cookie cookieFor(long userId, Role role) {
        return new Cookie(AuthCookies.COOKIE_NAME, tokenService.issue(userId, role));
    }
}
