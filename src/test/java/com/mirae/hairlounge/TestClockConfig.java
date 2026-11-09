package com.mirae.hairlounge;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트용 고정 시계(Asia/Seoul 2026-11-09 10:00)입니다. 날짜 경계 규칙을 늘 같은 시각으로 검사합니다(NFR8).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestClockConfig {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    public static final Instant FIXED_INSTANT = ZonedDateTime.of(2026, 11, 9, 10, 0, 0, 0, SEOUL).toInstant();

    @Bean
    @Primary
    Clock testClock() {
        return Clock.fixed(FIXED_INSTANT, SEOUL);
    }
}
