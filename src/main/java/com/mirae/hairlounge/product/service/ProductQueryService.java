package com.mirae.hairlounge.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mirae.hairlounge.common.error.NotFoundException;
import com.mirae.hairlounge.product.domain.Product;
import com.mirae.hairlounge.product.mapper.ProductMapper;
import com.mirae.hairlounge.product.mapper.ProductOptionMapper;

/**
 * 상품 조회입니다. 관리자와 직원 모두 씁니다(요구사항 2절).
 */
@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private final ProductMapper productMapper;
    private final ProductOptionMapper productOptionMapper;

    public ProductQueryService(ProductMapper productMapper, ProductOptionMapper productOptionMapper) {
        this.productMapper = productMapper;
        this.productOptionMapper = productOptionMapper;
    }

    public ProductDetail getProduct(long productId) {
        Product product = productMapper.findById(productId).orElseThrow(NotFoundException::new);
        return new ProductDetail(product, productOptionMapper.findByProductId(productId));
    }
}
