package com.mirae.hairlounge.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.mirae.hairlounge.TestClockConfig;

/**
 * 컨트롤러·보안 흐름 테스트가 함께 쓰는 설정(고정 시계, 테스트 사용자 도우미)입니다.
 */
@TestConfiguration(proxyBeanMethods = false)
@Import({TestClockConfig.class, TestUsers.class})
public class WebTestConfig {
}
