package com.mirae.hairlounge.common.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 앱 전체가 쓰는 시계입니다. 오늘·이번 달·연체 같은 날짜 경계는 이 시계(Asia/Seoul)로만 계산합니다(NFR8).
 */
@Configuration(proxyBeanMethods = false)
public class ClockConfig {

    @Bean
    Clock clock(@Value("${mirae.zone-id:Asia/Seoul}") String zoneId) {
        return Clock.system(ZoneId.of(zoneId));
    }
}
