package com.driply.backend.domains.product.entity;

import lombok.Getter;

/**
 * 상품 이미지 타입 enum
 */
@Getter
public enum ImageType {
    MAIN("메인 이미지"),         // 상품 슬라이드용 (썸네일 포함)
    DESCRIPTION("설명 이미지");   // 상품 설명 내 삽입용

    private final String description;

    ImageType(String description) {
        this.description = description;
    }

    /**
     * 메인 이미지(슬라이드)인지 확인
     */
    public boolean isMainImage() {
        return this == MAIN;
    }

    /**
     * 설명 이미지인지 확인
     */
    public boolean isDescriptionImage() {
        return this == DESCRIPTION;
    }
}