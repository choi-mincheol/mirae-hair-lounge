package com.mirae.hairlounge.auth.domain;

import java.time.LocalDateTime;

import com.mirae.hairlounge.common.security.Role;

/**
 * 사용자 계정(관리자, 직원)입니다. 비밀번호는 BCrypt 해시로만 둡니다(NFR3).
 */
public class UserAccount {

    private Long id;
    private String loginId;
    private String name;
    private String passwordHash;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** 비밀번호 해시는 로그에 남지 않도록 넣지 않습니다(NFR3). */
    @Override
    public String toString() {
        return "UserAccount{id=" + id + ", loginId=" + loginId + ", role=" + role + ", enabled=" + enabled + "}";
    }
}
