package com.mirae.hairlounge.common.security;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mirae.hairlounge.TestClockConfig;
import com.mirae.hairlounge.support.TestUsers;
import com.mirae.hairlounge.support.WebTestConfig;

import jakarta.servlet.http.Cookie;

/**
 * 실제 보안 필터 체인으로 로그인·JWT 쿠키·CSRF·만료·로그아웃 흐름을 검사합니다(FR1.1, FR1.5, NFR3, ADR-005, ADR-006, R-06).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(WebTestConfig.class)
@Transactional
@Sql("/sql/skeleton-products.sql")
class SecurityFlowTest {

    private static final String JWT_SECRET = "test-only-dummy-signing-key-not-a-real-secret-0123456789";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUsers testUsers;

    private long staffId;

    @BeforeEach
    void setUp() {
        staffId = testUsers.create("staff01", "김직원", Role.STAFF, true);
        testUsers.create("staff02", "이중지", Role.STAFF, false);
    }

    @Test
    void unauthenticatedRequestIsSentToLoginPage() throws Exception {
        mockMvc.perform(get("/products/1001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginPageIsOpenWithoutLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mirae 판매 관리")));
    }

    @Test
    void successfulLoginSetsHttpOnlyStrictCookieAndGoesToDashboard() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "staff01")
                        .param("password", TestUsers.PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString(AuthCookies.COOKIE_NAME + "="),
                        containsString("HttpOnly"),
                        containsString("SameSite=Strict"),
                        containsString("Path=/")))));
    }

    @Test
    void loginPostWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("loginId", "staff01")
                        .param("password", TestUsers.PASSWORD))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/denied"))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        everyItem(not(containsString(AuthCookies.COOKIE_NAME + "=ey")))));
    }

    @Test
    void deniedPageShowsNoPermissionMessageWith403() throws Exception {
        mockMvc.perform(get("/denied").cookie(testUsers.cookieFor(staffId, Role.STAFF)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("권한이 없습니다.")));
    }

    @Test
    void wrongPasswordShowsGenericMessageKeepsLoginIdAndSetsNoToken() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "staff01")
                        .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("아이디 또는 비밀번호가 올바르지 않습니다")))
                .andExpect(content().string(containsString("value=\"staff01\"")))
                .andExpect(content().string(not(containsString("wrong-password"))))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void disabledAccountGetsSameMessage() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "staff02")
                        .param("password", TestUsers.PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("아이디 또는 비밀번호가 올바르지 않습니다")))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void validCookieOpensProtectedPage() throws Exception {
        mockMvc.perform(get("/products/1001").cookie(testUsers.cookieFor(staffId, Role.STAFF)))
                .andExpect(status().isOk());
    }

    @Test
    void expiredTokenGoesToLoginWithExpiredNoticeAndOriginalAddress() throws Exception {
        Cookie expired = cookieIssuedBefore(Duration.ofHours(9));

        mockMvc.perform(get("/products/1001").cookie(expired))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expired&redirect=%2Fproducts%2F1001"))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        hasItem(allOf(containsString(AuthCookies.COOKIE_NAME + "="), containsString("Max-Age=0")))));
    }

    @Test
    void tokenSignedWithAnotherKeyIsTreatedLikeExpired() throws Exception {
        JwtTokenService forger = new JwtTokenService(new JwtProperties(
                "another-test-only-dummy-signing-key-not-a-secret-987654", Duration.ofHours(8), Duration.ofMinutes(5)),
                Clock.fixed(TestClockConfig.FIXED_INSTANT, TestClockConfig.SEOUL));
        Cookie forged = new Cookie(AuthCookies.COOKIE_NAME, forger.issue(staffId, Role.ADMIN));

        mockMvc.perform(get("/products/1001").cookie(forged))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expired&redirect=%2Fproducts%2F1001"));
    }

    @Test
    void loginPageShowsExpiredNoticeAndKeepsSafeRedirect() throws Exception {
        mockMvc.perform(get("/login").param("expired", "").param("redirect", "/products/1001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("로그인 시간이 지나 다시 로그인해 주세요.")))
                .andExpect(content().string(containsString("value=\"/products/1001\"")));
    }

    @Test
    void loginReturnsToSafeOriginalAddress() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("loginId", "staff01")
                        .param("password", TestUsers.PASSWORD)
                        .param("redirect", "/products/1001"))
                .andExpect(redirectedUrl("/products/1001"));
    }

    @Test
    void loginIgnoresExternalRedirectAddresses() throws Exception {
        for (String external : new String[] {"https://evil.example/", "//evil.example", "/\\evil.example", "products"}) {
            mockMvc.perform(post("/login").with(csrf())
                            .param("loginId", "staff01")
                            .param("password", TestUsers.PASSWORD)
                            .param("redirect", external))
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Test
    void tokenOlderThanFiveMinutesIsReissuedOnRequest() throws Exception {
        Cookie sixMinutesOld = cookieIssuedBefore(Duration.ofMinutes(6));

        mockMvc.perform(get("/products/1001").cookie(sixMinutesOld))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString(AuthCookies.COOKIE_NAME + "=ey"), containsString("HttpOnly")))));
    }

    @Test
    void freshTokenIsNotReissued() throws Exception {
        mockMvc.perform(get("/products/1001").cookie(testUsers.cookieFor(staffId, Role.STAFF)))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        everyItem(not(containsString(AuthCookies.COOKIE_NAME + "=")))));
    }

    @Test
    void logoutClearsTokenCookieAndShowsNotice() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()).cookie(testUsers.cookieFor(staffId, Role.STAFF)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        hasItem(allOf(containsString(AuthCookies.COOKIE_NAME + "="), containsString("Max-Age=0")))));

        mockMvc.perform(get("/login").param("logout", ""))
                .andExpect(content().string(containsString("로그아웃했습니다.")));
    }

    @Test
    void logoutWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/logout").cookie(testUsers.cookieFor(staffId, Role.STAFF)))
                .andExpect(status().isForbidden());
    }

    private Cookie cookieIssuedBefore(Duration age) {
        JwtTokenService past = new JwtTokenService(
                new JwtProperties(JWT_SECRET, Duration.ofHours(8), Duration.ofMinutes(5)),
                Clock.fixed(TestClockConfig.FIXED_INSTANT.minus(age), TestClockConfig.SEOUL));
        return new Cookie(AuthCookies.COOKIE_NAME, past.issue(staffId, Role.STAFF));
    }
}
