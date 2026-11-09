package com.mirae.hairlounge.common.security;

import java.io.Serializable;

/**
 * 로그인한 사용자입니다. 화면 위쪽 줄과 역할별 메뉴에 씁니다.
 */
public final class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String loginId;
    private final String displayName;
    private final Role role;

    public LoginUser(long id, String loginId, String displayName, Role role) {
        this.id = id;
        this.loginId = loginId;
        this.displayName = displayName;
        this.role = role;
    }

    public static LoginUser from(AccountStatus account) {
        return new LoginUser(account.id(), account.loginId(), account.name(), account.role());
    }

    public long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public String getRoleLabel() {
        return role.getLabel();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return loginId;
    }
}
