package com.driply.backend.domains.product.dto;

import com.driply.backend.domains.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 조회용 DTO (목록 + 상세 모두 사용)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long productId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer stockQuantity;
    private String category;
    private ProductStatus status;
    private String metadata;

    private Long sellerId;

    // 이미지들 (목록에서는 thumbnailUrl만, 상세에서는 모든 images)
    private List<ProductImageDTO> images;
    private String thumbnailUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && stockQuantity > 0;
    }
}