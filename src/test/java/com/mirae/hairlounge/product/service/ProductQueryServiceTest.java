package com.mirae.hairlounge.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.mirae.hairlounge.common.error.NotFoundException;
import com.mirae.hairlounge.product.domain.Product;
import com.mirae.hairlounge.product.domain.ProductOption;
import com.mirae.hairlounge.product.mapper.ProductMapper;
import com.mirae.hairlounge.product.mapper.ProductOptionMapper;

class ProductQueryServiceTest {

    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final ProductOptionMapper productOptionMapper = mock(ProductOptionMapper.class);
    private final ProductQueryService service = new ProductQueryService(productMapper, productOptionMapper);

    @Test
    void returnsProductWithItsOptions() {
        when(productMapper.findById(1L)).thenReturn(Optional.of(product(1L, "OO 염색약")));
        when(productOptionMapper.findByProductId(1L)).thenReturn(List.of(option("7N"), option("8N")));

        ProductDetail detail = service.getProduct(1L);

        assertThat(detail.product().getName()).isEqualTo("OO 염색약");
        assertThat(detail.options()).extracting(ProductOption::getName).containsExactly("7N", "8N");
    }

    @Test
    void throwsNotFoundForUnknownProduct() {
        when(productMapper.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProduct(404L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void returnsEmptyOptionListWhenProductHasNoOptions() {
        when(productMapper.findById(2L)).thenReturn(Optional.of(product(2L, "OO 샴푸")));
        when(productOptionMapper.findByProductId(2L)).thenReturn(List.of());

        assertThat(service.getProduct(2L).options()).isEmpty();
    }

    private static Product product(long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCategoryId(10L);
        product.setCategoryName("염색약");
        product.setBasePrice(9000L);
        product.setBaseCost(6000L);
        product.setActive(true);
        return product;
    }

    private static ProductOption option(String name) {
        ProductOption option = new ProductOption();
        option.setName(name);
        option.setPrice(9000L);
        option.setActive(true);
        return option;
    }
}
