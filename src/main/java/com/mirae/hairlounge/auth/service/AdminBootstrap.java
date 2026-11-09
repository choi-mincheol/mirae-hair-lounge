package com.mirae.hairlounge.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mirae.hairlounge.auth.domain.UserAccount;
import com.mirae.hairlounge.auth.mapper.UserAccountMapper;
import com.mirae.hairlounge.common.security.Role;

/**
 * 앱이 시작할 때 관리자 계정이 하나도 없으면 환경변수 값으로 관리자를 만듭니다(FR1.2).
 * 환경변수가 없으면 변수 이름을 알리고 시작을 멈춥니다.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    static final String LOGIN_ID_ENV = "MIRAE_ADMIN_LOGIN_ID";
    static final String PASSWORD_ENV = "MIRAE_ADMIN_PASSWORD";
    private static final String DEFAULT_NAME = "관리자";
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties properties;
    private final Clock clock;

    public AdminBootstrap(UserAccountMapper userAccountMapper, PasswordEncoder passwordEncoder,
            AdminProperties properties, Clock clock) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureAdminExists();
    }

    /** 관리자가 없을 때만 한 번 INSERT 합니다. 앱 시작 때 한 스레드에서만 불립니다. */
    public void ensureAdminExists() {
        if (userAccountMapper.countByRole(Role.ADMIN) > 0) {
            return;
        }
        List<String> missing = missingVariables();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("관리자 계정이 없어 시작할 수 없습니다. 환경변수 "
                    + String.join(", ", missing) + "를 설정한 뒤 다시 시작하세요.");
        }
        userAccountMapper.insert(newAdmin());
        log.info("첫 관리자 계정을 만들었습니다. loginId={}", properties.loginId().trim());
    }

    private List<String> missingVariables() {
        List<String> missing = new ArrayList<>();
        if (isBlank(properties.loginId())) {
            missing.add(LOGIN_ID_ENV);
        }
        if (isBlank(properties.password())) {
            missing.add(PASSWORD_ENV);
        }
        return missing;
    }

    private UserAccount newAdmin() {
        UserAccount admin = new UserAccount();
        admin.setLoginId(properties.loginId().trim());
        admin.setName(isBlank(properties.name()) ? DEFAULT_NAME : properties.name());
        admin.setPasswordHash(passwordEncoder.encode(properties.password()));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now(clock));
        return admin;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
