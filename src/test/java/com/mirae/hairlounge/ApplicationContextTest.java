package com.mirae.hairlounge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import com.mirae.hairlounge.common.security.UserAccountLookup;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestClockConfig.class)
class ApplicationContextTest {

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Clock clock;

    @Test
    void contextLoadsAndBindsOnlyToLoopbackAddress() {
        assertThat(environment.getProperty("server.address")).isEqualTo("127.0.0.1");
        assertThat(environment.getProperty("server.port")).isEqualTo("8080");
    }

    @Test
    void contextStartsWithoutAllowingCircularReferences() {
        // R-01: 순환 주입을 허용하지 않은 기본값 그대로 컨텍스트가 떠야 합니다.
        assertThat(environment.getProperty("spring.main.allow-circular-references", Boolean.class, false)).isFalse();
        assertThat(context.getBeansOfType(UserAccountLookup.class)).hasSize(1);
    }

    @Test
    void applicationClockUsesSeoulZoneAndTestsUseFixedClock() {
        Clock applicationClock = context.getBean("clock", Clock.class);

        assertThat(applicationClock.getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
        assertThat(clock.instant()).isEqualTo(TestClockConfig.FIXED_INSTANT);
    }

    @Test
    void startupFailsWhenJwtSecretIsMissing() {
        assertThatThrownBy(() -> startWith("--mirae.jwt.secret=", "--spring.datasource.url=jdbc:h2:mem:ctx-no-secret"))
                .hasStackTraceContaining("MIRAE_JWT_SECRET");
    }

    @Test
    void startupFailsWhenJwtSecretIsTooShort() {
        assertThatThrownBy(() -> startWith("--mirae.jwt.secret=short-key", "--spring.datasource.url=jdbc:h2:mem:ctx-short-secret"))
                .hasStackTraceContaining("32바이트");
    }

    private static void startWith(String... extraArgs) {
        String[] args = new String[extraArgs.length + 2];
        args[0] = "--spring.profiles.active=test";
        args[1] = "--server.port=0";
        System.arraycopy(extraArgs, 0, args, 2, extraArgs.length);
        SpringApplication.run(MiraeHairLoungeApplication.class, args).close();
    }
}
