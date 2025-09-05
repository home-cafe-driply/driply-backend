package com.driply.backend.domains.product.dto;

import com.driply.backend.domains.product.entity.ImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 이미지용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {

    private Long imageId;
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isThumbnail;
    private ImageType imageType;
}