package com.mirae.hairlounge.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import jakarta.servlet.DispatcherType;

/**
 * 보안 필터 체인(Common). 서버에 세션을 두지 않고 JWT 쿠키로 로그인 상태를 확인합니다(ADR-005, ADR-006).
 * 상태를 바꾸는 모든 요청(로그인 폼 포함)은 CSRF 토큰을 검사하고, 토큰은 쿠키 저장소에 둡니다.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    static final String ACCESS_DENIED_PATH = "/denied";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenService tokenService,
            UserAccountLookup userAccountLookup, AuthCookies authCookies) throws Exception {
        SecurityContextRepository contextRepository = new RequestAttributeSecurityContextRepository();
        JwtCookieFilter jwtCookieFilter = new JwtCookieFilter(tokenService, userAccountLookup, authCookies,
                contextRepository);

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(context -> context.securityContextRepository(contextRepository))
                .requestCache(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository()))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler((request, response, authentication) -> authCookies.clear(response))
                        .logoutSuccessUrl("/login?logout"))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/login", "/error", ACCESS_DENIED_PATH,
                                "/webjars/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new LoginRedirectEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static CookieCsrfTokenRepository csrfTokenRepository() {
        // 화면의 $.ajax가 meta 태그 값을 X-XSRF-TOKEN 머리글로 보냅니다. 인증 정보가 아니므로 HttpOnly가 아니어도 됩니다.
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.sameSite("Strict").path("/"));
        return repository;
    }

    private static AccessDeniedHandlerImpl accessDeniedHandler() {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        handler.setErrorPage(ACCESS_DENIED_PATH);
        return handler;
    }
}
