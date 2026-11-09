package com.mirae.hairlounge.product.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.mirae.hairlounge.product.domain.ProductOption;

/**
 * product_option 테이블은 Product만 씁니다(ADR-003).
 */
@Mapper
public interface ProductOptionMapper {

    List<ProductOption> findByProductId(long productId);
}
