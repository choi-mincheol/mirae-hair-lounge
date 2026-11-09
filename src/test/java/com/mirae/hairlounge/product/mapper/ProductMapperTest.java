package com.mirae.hairlounge.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.mirae.hairlounge.product.domain.Product;
import com.mirae.hairlounge.product.domain.ProductOption;

@MybatisTest
@ActiveProfiles("test")
@Sql("/sql/skeleton-products.sql")
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductOptionMapper productOptionMapper;

    @Test
    void findsProductWithCategoryName() {
        Product product = productMapper.findById(1001L).orElseThrow();

        assertThat(product.getName()).isEqualTo("OO 염색약");
        assertThat(product.getCategoryId()).isEqualTo(1001L);
        assertThat(product.getCategoryName()).isEqualTo("염색약");
        assertThat(product.getBrand()).isEqualTo("OO");
        assertThat(product.getBasePrice()).isEqualTo(9000L);
        assertThat(product.getBaseCost()).isEqualTo(6000L);
        assertThat(product.isActive()).isTrue();
    }

    @Test
    void findsOptionsOfProductInIdOrder() {
        List<ProductOption> options = productOptionMapper.findByProductId(1001L);

        assertThat(options).extracting(ProductOption::getName).containsExactly("7N", "8N");
        ProductOption sevenN = options.get(0);
        assertThat(sevenN.getBarcode()).isEqualTo("8801000000071");
        assertThat(sevenN.getPrice()).isEqualTo(9000L);
        assertThat(sevenN.getMinStock()).isEqualTo(5);
        assertThat(sevenN.getCurrentStock()).isEqualTo(2);
        assertThat(sevenN.isActive()).isTrue();
        ProductOption eightN = options.get(1);
        assertThat(eightN.getBarcode()).isNull();
        assertThat(eightN.isActive()).isFalse();
    }

    @Test
    void returnsEmptyForUnknownProductId() {
        assertThat(productMapper.findById(9999L)).isEmpty();
    }

    @Test
    void productWithoutOptionsHasEmptyOptionList() {
        Product shampoo = productMapper.findById(1002L).orElseThrow();

        assertThat(shampoo.getBrand()).isNull();
        assertThat(shampoo.isActive()).isFalse();
        assertThat(productOptionMapper.findByProductId(1002L)).isEmpty();
    }
}
