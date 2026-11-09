package com.mirae.hairlounge.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mirae.hairlounge.auth.domain.UserAccount;
import com.mirae.hairlounge.auth.mapper.UserAccountMapper;
import com.mirae.hairlounge.common.security.LoginUser;

/**
 * 아이디와 비밀번호를 BCrypt 해시와 비교해 로그인시킵니다(FR1.1).
 */
@Service
public class LoginService {

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    /** 없는 아이디도 같은 시간만큼 비교해 아이디가 있는지 응답 시간으로 드러나지 않게 합니다. */
    private final String dummyHash;

    public LoginService(UserAccountMapper userAccountMapper, PasswordEncoder passwordEncoder) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-equal-timing");
    }

    @Transactional(readOnly = true)
    public LoginUser login(String loginId, String rawPassword) {
        if (loginId == null || loginId.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            throw new LoginFailedException();
        }
        Optional<UserAccount> found = userAccountMapper.findByLoginId(loginId.trim());
        boolean passwordMatches = passwordEncoder.matches(rawPassword,
                found.map(UserAccount::getPasswordHash).orElse(dummyHash));
        UserAccount account = found.filter(a -> passwordMatches && a.isEnabled())
                .orElseThrow(LoginFailedException::new);
        return new LoginUser(account.getId(), account.getLoginId(), account.getName(), account.getRole());
    }
}
