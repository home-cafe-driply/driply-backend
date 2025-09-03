package com.driply.backend.domains.product.entity;

import lombok.Getter;

/**
 * 상품 상태 enum
 */
@Getter
public enum ProductStatus {
    ACTIVE("판매중"),
    INACTIVE("판매중지"),
    DELETED("삭제됨");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    /**
     * 판매 가능한 상태인지 확인
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 비활성 상태인지 확인
     */
    public boolean isInactive() {
        return this == INACTIVE;
    }

    /**
     * 삭제된 상태인지 확인
     */
    public boolean isDeleted() {
        return this == DELETED;
    }
}