package com.mirae.hairlounge.product.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mirae.hairlounge.common.security.Role;
import com.mirae.hairlounge.support.TestUsers;
import com.mirae.hairlounge.support.WebTestConfig;

import jakarta.servlet.http.Cookie;

/**
 * 상품 1건 조회 화면(/products/{id})을 검사합니다(FR1.4, FR2.2, FR3.5, NFR7, S20).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(WebTestConfig.class)
@Transactional
@Sql("/sql/skeleton-products.sql")
class ProductViewControllerTest {

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
    void adminSeesProductWithOptionsPricesAndStockStatus() throws Exception {
        mockMvc.perform(get("/products/1001").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(view().name("product/detail"))
                .andExpect(content().string(containsString("OO 염색약")))
                .andExpect(content().string(containsString("염색약")))
                .andExpect(content().string(containsString("7N")))
                .andExpect(content().string(containsString("8N")))
                .andExpect(content().string(containsString("9,000원")))
                .andExpect(content().string(containsString("9,500원")))
                .andExpect(content().string(containsString("재고 부족")))
                .andExpect(content().string(containsString("판매 중지")));
    }

    @Test
    void costPriceIsNotShownOnDetailScreen() throws Exception {
        mockMvc.perform(get("/products/1001").cookie(staffCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("6,000원"))));
    }

    @Test
    void staffCanViewProductToo() throws Exception {
        mockMvc.perform(get("/products/1001").cookie(staffCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("OO 염색약")));
    }

    @Test
    void productWithoutOptionsSaysSo() throws Exception {
        mockMvc.perform(get("/products/1002").cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("OO 샴푸")))
                .andExpect(content().string(containsString("등록된 옵션이 없습니다.")));
    }

    @Test
    void unknownProductShowsNotFoundScreen() throws Exception {
        mockMvc.perform(get("/products/999999").cookie(adminCookie))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("요청한 화면이나 자료를 찾을 수 없습니다.")));
    }

    @Test
    void nonNumericProductIdShowsNotFoundWithoutTechnicalDetails() throws Exception {
        mockMvc.perform(get("/products/abc").cookie(adminCookie))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("요청한 화면이나 자료를 찾을 수 없습니다.")))
                .andExpect(content().string(not(containsString("Exception"))));
    }
}
