package com.mirae.hairlounge.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.mirae.hairlounge.TestClockConfig;

/**
 * 실제 서버(127.0.0.1, 임의 포트)로 CSRF 쿠키 저장소와 JWT 쿠키를 끝까지 검사합니다(ADR-005, NFR1).
 * 폼은 숨은 칸으로, $.ajax는 meta 태그 값을 X-XSRF-TOKEN 머리글로 보냅니다.
 * 로그인은 test 프로필의 더미 첫 관리자 계정을 씁니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestClockConfig.class)
class CsrfCookieServerTest {

    private static final Pattern HIDDEN_CSRF = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");
    private static final Pattern META_CSRF = Pattern.compile("<meta name=\"_csrf\" content=\"([^\"]+)\"");

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://127.0.0.1:" + port;
    }

    @Test
    void loginPageIssuesStrictCsrfCookieReadableByScripts() throws Exception {
        HttpResponse<String> loginPage = get("/login", null);

        String csrfCookie = setCookie(loginPage, "XSRF-TOKEN");
        assertThat(csrfCookie).contains("SameSite=Strict").contains("Path=/").doesNotContain("HttpOnly");
        assertThat(find(HIDDEN_CSRF, loginPage.body())).isNotBlank();
    }

    @Test
    void formLoginWithHiddenTokenSetsHttpOnlyJwtCookie() throws Exception {
        Session session = login();

        assertThat(session.authSetCookie).contains("HttpOnly").contains("SameSite=Strict").contains("Path=/");
    }

    @Test
    void pagesTellScriptsToSendXsrfHeader() throws Exception {
        Session session = login();

        HttpResponse<String> dashboard = get("/", session.cookieHeader());

        assertThat(dashboard.statusCode()).isEqualTo(200);
        assertThat(dashboard.body()).contains("<meta name=\"_csrf_header\" content=\"X-XSRF-TOKEN\"");
    }

    @Test
    void postWithXsrfHeaderFromMetaTagIsAccepted() throws Exception {
        Session session = login();
        String metaToken = find(META_CSRF, get("/", session.cookieHeader()).body());

        HttpResponse<String> logout = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/logout"))
                .header("Cookie", session.cookieHeader())
                .header("X-XSRF-TOKEN", metaToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(), HttpResponse.BodyHandlers.ofString());

        assertThat(logout.statusCode()).isEqualTo(302);
        assertThat(logout.headers().firstValue("Location")).get().asString().endsWith("/login?logout");
    }

    @Test
    void postWithoutCsrfTokenIsForbiddenAndShowsNoPermissionScreen() throws Exception {
        Session session = login();

        HttpResponse<String> logout = client.send(HttpRequest.newBuilder(URI.create(baseUrl + "/logout"))
                .header("Cookie", session.cookieHeader())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(), HttpResponse.BodyHandlers.ofString());

        assertThat(logout.statusCode()).isEqualTo(403);
        assertThat(logout.body()).contains("권한이 없습니다.").doesNotContain("Exception");
    }

    @Test
    void wrongCsrfTokenOnLoginIsForbidden() throws Exception {
        HttpResponse<String> loginPage = get("/login", null);
        String csrfCookieValue = cookieValue(setCookie(loginPage, "XSRF-TOKEN"));

        HttpResponse<String> response = postForm("/login", "XSRF-TOKEN=" + csrfCookieValue,
                "loginId=test-admin&password=test-admin-password&_csrf=forged-token");

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.headers().allValues("Set-Cookie")).noneMatch(c -> c.startsWith(AuthCookies.COOKIE_NAME + "=ey"));
    }

    private Session login() throws IOException, InterruptedException {
        HttpResponse<String> loginPage = get("/login", null);
        String csrfCookieValue = cookieValue(setCookie(loginPage, "XSRF-TOKEN"));
        String formToken = find(HIDDEN_CSRF, loginPage.body());

        HttpResponse<String> response = postForm("/login", "XSRF-TOKEN=" + csrfCookieValue,
                "loginId=test-admin&password=test-admin-password&_csrf="
                        + URLEncoder.encode(formToken, StandardCharsets.UTF_8));

        assertThat(response.statusCode()).isEqualTo(302);
        String authSetCookie = setCookie(response, AuthCookies.COOKIE_NAME);
        return new Session(cookieValue(authSetCookie), csrfCookieValue, authSetCookie);
    }

    private HttpResponse<String> get(String path, String cookieHeader) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET();
        if (cookieHeader != null) {
            builder.header("Cookie", cookieHeader);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postForm(String path, String cookieHeader, String body)
            throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", cookieHeader)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String setCookie(HttpResponse<?> response, String name) {
        List<String> cookies = response.headers().allValues("Set-Cookie");
        return cookies.stream()
                .filter(cookie -> cookie.startsWith(name + "=") && !cookie.startsWith(name + "=;"))
                .findFirst()
                .orElseThrow(() -> new AssertionError(name + " 쿠키가 없습니다: " + cookies));
    }

    private static String cookieValue(String setCookie) {
        int start = setCookie.indexOf('=') + 1;
        int end = setCookie.indexOf(';');
        return setCookie.substring(start, end < 0 ? setCookie.length() : end);
    }

    private static String find(Pattern pattern, String body) {
        Matcher matcher = pattern.matcher(body);
        assertThat(matcher.find()).as("pattern %s", pattern).isTrue();
        return matcher.group(1);
    }

    private record Session(String authToken, String csrfCookie, String authSetCookie) {

        String cookieHeader() {
            return AuthCookies.COOKIE_NAME + "=" + authToken + "; XSRF-TOKEN=" + csrfCookie;
        }
    }
}
