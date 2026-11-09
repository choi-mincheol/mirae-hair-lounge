package com.mirae.hairlounge.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import com.mirae.hairlounge.TestClockConfig;

/**
 * dev 프로필에서만 예시 분류·상품 1건이 들어가고, 다시 실행해도 늘지 않는지 검사합니다(계획 Step 9).
 * 다른 테스트와 DB가 섞이지 않게 따로 이름 붙인 인메모리 DB를 씁니다.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:dev-profile-data")
@ActiveProfiles({"test", "dev"})
@Import(TestClockConfig.class)
class DevProfileDataTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void devProfileLoadsOneSampleProductWithOptions() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        assertThat(countSampleProducts(jdbc)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM product_option o JOIN product p ON p.id = o.product_id"
                + " WHERE p.name = '예시 염색약'", Integer.class)).isEqualTo(2);
    }

    @Test
    void runningSampleDataAgainDoesNotDuplicate() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("data-dev.sql"));
        populator.setSqlScriptEncoding("UTF-8");

        populator.execute(dataSource);
        populator.execute(dataSource);

        assertThat(countSampleProducts(jdbc)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM category WHERE name = '염색약'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void defaultConfigurationDoesNotLoadSampleData() {
        assertThat(new ClassPathResource("application.yml").exists()).isTrue();
        assertThat(readClasspath("application.yml")).doesNotContain("data-dev.sql");
        assertThat(readClasspath("application-test.yml")).doesNotContain("data-dev.sql");
    }

    private static int countSampleProducts(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM product WHERE name = '예시 염색약'", Integer.class);
    }

    private static String readClasspath(String name) {
        try {
            return new ClassPathResource(name).getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
