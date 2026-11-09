package com.mirae.hairlounge.product.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.mirae.hairlounge.product.domain.Product;

/**
 * product 테이블은 Product만 씁니다(ADR-003).
 */
@Mapper
public interface ProductMapper {

    Optional<Product> findById(long id);
}
