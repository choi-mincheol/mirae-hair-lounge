package com.mirae.hairlounge.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 뼈대 테이블(user_account, category, product, product_option)과 유일 제약을 검사합니다.
 */
@MybatisTest
@ActiveProfiles("test")
class SchemaTest {

    private static final Timestamp NOW = Timestamp.valueOf(LocalDateTime.of(2026, 11, 9, 10, 0));

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
    }

    @Test
    void skeletonTablesExist() {
        for (String table : new String[] {"USER_ACCOUNT", "CATEGORY", "PRODUCT", "PRODUCT_OPTION"}) {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = ?", Integer.class, table);
            assertThat(count).as(table).isEqualTo(1);
        }
    }

    @Test
    void moneyColumnsAreWholeNumberBigint() {
        for (String[] column : new String[][] {
                {"PRODUCT", "BASE_PRICE"}, {"PRODUCT", "BASE_COST"},
                {"PRODUCT_OPTION", "PRICE"}, {"PRODUCT_OPTION", "COST"}}) {
            String type = jdbc.queryForObject(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = ? AND UPPER(COLUMN_NAME) = ?",
                    String.class, column[0], column[1]);
            assertThat(type).as(column[0] + "." + column[1]).isEqualTo("BIGINT");
        }
    }

    @Test
    void loginIdMustBeUnique() {
        insertUser("staff01");

        assertThatThrownBy(() -> insertUser("staff01")).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void categoryNameMustBeUnique() {
        insertCategory("염색약");

        assertThatThrownBy(() -> insertCategory("염색약")).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void optionNameMustBeUniqueWithinProductOnly() {
        long categoryId = insertCategory("염색약");
        long dyeA = insertProduct(categoryId, "OO 염색약");
        long dyeB = insertProduct(categoryId, "XX 염색약");
        insertOption(dyeA, "7N", null);

        assertThatThrownBy(() -> insertOption(dyeA, "7N", null)).isInstanceOf(DuplicateKeyException.class);
        assertThatCode(() -> insertOption(dyeB, "7N", null)).doesNotThrowAnyException();
    }

    @Test
    void barcodeMustBeUniqueButMayBeEmpty() {
        long categoryId = insertCategory("샴푸");
        long shampoo = insertProduct(categoryId, "OO 샴푸");
        insertOption(shampoo, "300ml", "8801234567890");

        assertThatThrownBy(() -> insertOption(shampoo, "500ml", "8801234567890"))
                .isInstanceOf(DuplicateKeyException.class);
        assertThatCode(() -> {
            insertOption(shampoo, "1000ml", null);
            insertOption(shampoo, "리필", null);
        }).doesNotThrowAnyException();
    }

    @Test
    void currentStockAndMoneyCannotBeNegative() {
        long categoryId = insertCategory("트리트먼트");
        long productId = insertProduct(categoryId, "OO 트리트먼트");

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO product_option (product_id, name, price, cost, min_stock, current_stock, active, created_at, updated_at)"
                        + " VALUES (?, ?, 1000, 500, 0, -1, TRUE, ?, ?)", productId, "기본", NOW, NOW))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO product (name, category_id, brand, base_price, base_cost, active, created_at, updated_at)"
                        + " VALUES (?, ?, NULL, -1, 0, TRUE, ?, ?)", "음수 가격", categoryId, NOW, NOW))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertUser(String loginId) {
        jdbc.update("INSERT INTO user_account (login_id, name, password_hash, role, enabled, created_at)"
                + " VALUES (?, ?, ?, 'STAFF', TRUE, ?)", loginId, "직원", "{bcrypt}dummy-hash", NOW);
    }

    private long insertCategory(String name) {
        jdbc.update("INSERT INTO category (name, created_at) VALUES (?, ?)", name, NOW);
        return jdbc.queryForObject("SELECT id FROM category WHERE name = ?", Long.class, name);
    }

    private long insertProduct(long categoryId, String name) {
        jdbc.update("INSERT INTO product (name, category_id, brand, base_price, base_cost, active, created_at, updated_at)"
                + " VALUES (?, ?, 'OO', 9000, 6000, TRUE, ?, ?)", name, categoryId, NOW, NOW);
        return jdbc.queryForObject("SELECT id FROM product WHERE name = ?", Long.class, name);
    }

    private void insertOption(long productId, String name, String barcode) {
        jdbc.update("INSERT INTO product_option (product_id, name, barcode, price, cost, min_stock, current_stock, active,"
                + " created_at, updated_at) VALUES (?, ?, ?, 9000, 6000, 0, 0, TRUE, ?, ?)",
                productId, name, barcode, NOW, NOW);
    }
}
