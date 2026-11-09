package com.mirae.hairlounge.product.domain;

import java.time.LocalDateTime;

/**
 * 상품 옵션(예: 염색약 7N)입니다. 재고와 판매는 옵션 단위로 셉니다(FR2.2, ADR-004).
 */
public class ProductOption {

    private Long id;
    private Long productId;
    private String name;
    private String barcode;
    private long price;
    private long cost;
    private int minStock;
    private int currentStock;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 현재 재고가 최소 재고 이하이면 재고 부족입니다(FR3.5). */
    public boolean isLowStock() {
        return currentStock <= minStock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public int getMinStock() {
        return minStock;
    }

    public void setMinStock(int minStock) {
        this.minStock = minStock;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
