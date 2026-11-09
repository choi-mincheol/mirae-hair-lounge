package com.mirae.hairlounge.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mirae.hairlounge.product.service.ProductQueryService;

/**
 * 상품 1건 조회 화면입니다. 관리자와 직원 모두 볼 수 있습니다(요구사항 2절).
 */
@Controller
public class ProductViewController {

    private final ProductQueryService productQueryService;

    public ProductViewController(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("detail", productQueryService.getProduct(id));
        return "product/detail";
    }
}
