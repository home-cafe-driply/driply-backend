package com.driply.backend.domains.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 등록/수정용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer stockQuantity;
    private String category;
    private String metadata;
}