package com.driply.backend.domains.product.entity;

import com.driply.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Table(name = "product_images")
@ToString(callSuper = true, of = {"imageId", "productId", "imageUrl", "sortOrder", "isThumbnail"})
public class ProductImageEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "alt_text", length = 200)
    private String altText;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_thumbnail", nullable = false)
    @Builder.Default
    private Boolean isThumbnail = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    @Builder.Default
    private ImageType imageType = ImageType.MAIN;

    /**
     * 썸네일 이미지로 설정
     */
    public void setAsThumbnail() {
        this.isThumbnail = true;
    }

    /**
     * 일반 이미지로 변경
     */
    public void unsetThumbnail() {
        this.isThumbnail = false;
    }

    /**
     * 정렬 순서 변경
     */
    public void changeSortOrder(int newOrder) {
        this.sortOrder = newOrder;
    }

    /**
     * Alt 텍스트 업데이트
     */
    public void updateAltText(String newAltText) {
        this.altText = newAltText;
    }

    /**
     * 썸네일 여부 확인
     */
    public boolean isThumbnailImage() {
        return this.isThumbnail != null && this.isThumbnail;
    }

    /**
     * 유효한 이미지인지 확인 (논리적 삭제되지 않은 상태)
     */
    public boolean isValidImage() {
        return this.isActive(); // BaseEntity의 메서드 활용
    }

    /**
     * 메인 이미지인지 확인
     */
    public boolean isMainImage() {
        return this.imageType != null && this.imageType.isMainImage();
    }

    /**
     * 설명 이미지인지 확인
     */
    public boolean isDescriptionImage() {
        return this.imageType != null && this.imageType.isDescriptionImage();
    }

    /**
     * 이미지 타입 변경
     */
    public void changeImageType(ImageType newType) {
        this.imageType = newType;
        // 설명 이미지는 썸네일이 될 수 없음
        if (newType == ImageType.DESCRIPTION) {
            this.isThumbnail = false;
        }
    }
}