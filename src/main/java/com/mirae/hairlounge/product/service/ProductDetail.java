package com.mirae.hairlounge.product.service;

import java.util.List;

import com.mirae.hairlounge.product.domain.Product;
import com.mirae.hairlounge.product.domain.ProductOption;

/**
 * 상품 1건과 그 옵션들입니다.
 */
public record ProductDetail(Product product, List<ProductOption> options) {

    public ProductDetail {
        options = List.copyOf(options);
    }
}
