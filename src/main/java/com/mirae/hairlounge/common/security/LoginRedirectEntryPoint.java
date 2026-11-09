package com.mirae.hairlounge.common.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인하지 않은 요청을 로그인 화면으로 보냅니다(FR1.1).
 * 토큰이 만료되었거나 거부된 경우에는 만료 안내와 원래 주소(redirect)를 함께 넘깁니다(FR1.5, R-06).
 */
public class LoginRedirectEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.sendRedirect(request.getContextPath() + target(request));
    }

    private static String target(HttpServletRequest request) {
        if (!Boolean.TRUE.equals(request.getAttribute(JwtCookieFilter.TOKEN_REJECTED_ATTRIBUTE))) {
            return "/login";
        }
        String original = originalPath(request);
        if (!"GET".equalsIgnoreCase(request.getMethod()) || !RedirectTargets.isSafe(original)) {
            return "/login?expired";
        }
        return "/login?expired&redirect=" + URLEncoder.encode(original, StandardCharsets.UTF_8);
    }

    private static String originalPath(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String query = request.getQueryString();
        return query == null ? path : path + "?" + query;
    }
}
