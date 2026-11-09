package com.mirae.hairlounge.common.security;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 쿠키를 읽고 씁니다. HttpOnly, SameSite=Strict, Path=/ 이고, http로만 열리므로 Secure는 켜지 않습니다(ADR-005).
 */
@Component
public class AuthCookies {

    public static final String COOKIE_NAME = "MIRAE_AUTH";

    private final JwtTokenService tokenService;

    public AuthCookies(JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    public void write(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, base(token).maxAge(tokenService.expiry()).build().toString());
    }

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, base("").maxAge(0).build().toString());
    }

    private static ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/");
    }
}
