package com.mirae.hairlounge.common;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.mirae.hairlounge.common.security.Role;
import com.mirae.hairlounge.support.TestUsers;
import com.mirae.hairlounge.support.WebTestConfig;

import jakarta.servlet.http.Cookie;

/**
 * 공통 화면 틀(왼쪽 메뉴, 역할별 메뉴, CSRF meta 태그)과 로그인 화면의 data-testid를 검사합니다(FR10.1, FR1.4, ADR-005).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(WebTestConfig.class)
@Transactional
@Sql("/sql/skeleton-products.sql")
class LayoutViewTest {

    private static final String[] COMMON_MENUS = {"dashboard", "sales", "sale-new", "products", "clients"};
    private static final String[] ADMIN_ONLY_MENUS = {"categories", "receipts", "approvals", "reports", "users"};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUsers testUsers;

    private Cookie adminCookie;
    private Cookie staffCookie;

    @BeforeEach
    void setUp() {
        adminCookie = testUsers.cookieFor(testUsers.create("admin01", "홍길동", Role.ADMIN, true), Role.ADMIN);
        staffCookie = testUsers.cookieFor(testUsers.create("staff01", "김직원", Role.STAFF, true), Role.STAFF);
    }

    @Test
    void adminSeesSidebarWithAllAdminMenus() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/1001").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<nav aria-label=\"주 메뉴\"")))
                .andExpect(content().string(not(containsString("data-testid=\"nav-my-requests\""))));
        for (String menu : COMMON_MENUS) {
            result.andExpect(content().string(containsString("data-testid=\"nav-" + menu + "\"")));
        }
        for (String menu : ADMIN_ONLY_MENUS) {
            result.andExpect(content().string(containsString("data-testid=\"nav-" + menu + "\"")));
        }
    }

    @Test
    void staffDoesNotSeeAdminOnlyMenusButSeesOwnRequests() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/1001").cookie(staffCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-testid=\"nav-my-requests\"")));
        for (String menu : COMMON_MENUS) {
            result.andExpect(content().string(containsString("data-testid=\"nav-" + menu + "\"")));
        }
        for (String menu : ADMIN_ONLY_MENUS) {
            result.andExpect(content().string(not(containsString("data-testid=\"nav-" + menu + "\""))));
        }
    }

    @Test
    void currentMenuIsMarkedForScreenReaders() throws Exception {
        mockMvc.perform(get("/products/1001").cookie(adminCookie))
                .andExpect(content().string(containsString("data-testid=\"nav-products\" aria-current=\"page\"")))
                .andExpect(content().string(not(containsString("data-testid=\"nav-dashboard\" aria-current=\"page\""))));
    }

    @Test
    void headerShowsUserNameRoleAndCsrfProtectedLogout() throws Exception {
        mockMvc.perform(get("/products/1001").cookie(adminCookie))
                .andExpect(content().string(containsString("홍길동(관리자)")))
                .andExpect(content().string(containsString("data-testid=\"header-password-link\"")))
                .andExpect(content().string(containsString("data-testid=\"header-logout-button\"")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void layoutHasCsrfMetaTagsForAjax() throws Exception {
        // 머리글 이름(X-XSRF-TOKEN)은 실제 서버로 CsrfCookieServerTest가 확인합니다.
        // MockMvc의 csrf() 도우미가 테스트 중에 CSRF 저장소를 바꾸기 때문입니다.
        mockMvc.perform(get("/products/1001").cookie(staffCookie))
                .andExpect(content().string(containsString("<meta name=\"_csrf\" content=\"")))
                .andExpect(content().string(containsString("<meta name=\"_csrf_header\" content=\"X-")));
    }

    @Test
    void layoutLoadsBootstrapJqueryAndAppAssetsFromTheApp() throws Exception {
        mockMvc.perform(get("/products/1001").cookie(staffCookie))
                .andExpect(content().string(containsString("href=\"/webjars/bootstrap/")))
                .andExpect(content().string(containsString("/css/bootstrap.min.css\"")))
                .andExpect(content().string(containsString("/js/bootstrap.bundle.min.js\"")))
                .andExpect(content().string(containsString("src=\"/webjars/jquery/")))
                .andExpect(content().string(containsString("/jquery.min.js\"")))
                .andExpect(content().string(containsString("/css/app.css")))
                .andExpect(content().string(containsString("/js/common.js")))
                .andExpect(content().string(not(containsString("cdn."))));
    }

    @Test
    void staticAssetsAreServedWithoutLogin() throws Exception {
        mockMvc.perform(get("/webjars/bootstrap/css/bootstrap.min.css")).andExpect(status().isOk());
        mockMvc.perform(get("/webjars/jquery/jquery.min.js")).andExpect(status().isOk());
        mockMvc.perform(get("/css/app.css")).andExpect(status().isOk());
        mockMvc.perform(get("/js/common.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("$.ajaxSetup")))
                .andExpect(content().string(containsString("X-XSRF-TOKEN")));
    }

    @Test
    void loginPageHasTestIdsAndNoSidebar() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(content().string(containsString("data-testid=\"login-form\"")))
                .andExpect(content().string(containsString("data-testid=\"login-id-input\"")))
                .andExpect(content().string(containsString("data-testid=\"login-password-input\"")))
                .andExpect(content().string(containsString("data-testid=\"login-submit-button\"")))
                .andExpect(content().string(containsString("<label for=\"loginId\"")))
                .andExpect(content().string(not(containsString("aria-label=\"주 메뉴\""))));
    }

    @Test
    void loginErrorIsShownInTestableMessageArea() throws Exception {
        mockMvc.perform(post("/login").with(csrf()).param("loginId", "admin01").param("password", "wrong"))
                .andExpect(content().string(containsString("data-testid=\"login-error-message\"")));
    }

    @Test
    void notFoundScreenUsesCommonLayoutForLoggedInUser() throws Exception {
        mockMvc.perform(get("/products/999999").cookie(staffCookie))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("aria-label=\"주 메뉴\"")))
                .andExpect(content().string(containsString("data-testid=\"error-home-link\"")));
    }
}
