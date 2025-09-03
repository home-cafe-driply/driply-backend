package com.driply.backend.domains.product.repository;

import com.driply.backend.domains.product.entity.ImageType;
import com.driply.backend.domains.product.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

    /**
     * 상품의 활성 이미지 목록 조회 (정렬 순서별)
     */
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.productId = :productId AND pi.deleted = false " +
            "ORDER BY pi.sortOrder ASC, pi.createdAt ASC")
    List<ProductImageEntity> findActiveImagesByProductId(@Param("productId") Long productId);

    /**
     * 상품의 특정 타입 이미지들 조회
     */
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.productId = :productId AND pi.deleted = false " +
            "AND pi.imageType = :imageType ORDER BY pi.sortOrder ASC, pi.createdAt ASC")
    List<ProductImageEntity> findActiveImagesByProductIdAndType(@Param("productId") Long productId,
                                                                @Param("imageType") ImageType imageType);

    /**
     * 상품의 메인 이미지들 조회 (슬라이드용)
     */
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.productId = :productId AND pi.deleted = false " +
            "AND pi.imageType = 'MAIN' ORDER BY pi.sortOrder ASC, pi.createdAt ASC")
    List<ProductImageEntity> findMainImagesByProductId(@Param("productId") Long productId);

    /**
     * 상품의 설명 이미지들 조회
     */
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.productId = :productId AND pi.deleted = false " +
            "AND pi.imageType = 'DESCRIPTION' ORDER BY pi.sortOrder ASC, pi.createdAt ASC")
    List<ProductImageEntity> findDescriptionImagesByProductId(@Param("productId") Long productId);

    /**
     * 상품의 썸네일 이미지 조회 (메인 이미지 중에서만)
     */
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.productId = :productId AND pi.deleted = false " +
            "AND pi.imageType = 'MAIN' AND pi.isThumbnail = true ORDER BY pi.sortOrder ASC")
    Optional<ProductImageEntity> findThumbnailByProductId(@Param("productId") Long productId);

    /**
     * 상품의 첫 번째 메인 이미지 조회 (썸네일이 없을 경우 대체용)
     */
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.productId = :productId AND pi.deleted = false " +
            "AND pi.imageType = 'MAIN' ORDER BY pi.sortOrder ASC, pi.createdAt ASC LIMIT 1")
    Optional<ProductImageEntity> findFirstMainImageByProductId(@Param("productId") Long productId);

    /**
     * 상품의 활성 이미지 개수 조회
     */
    @Query("SELECT COUNT(pi) FROM ProductImageEntity pi WHERE pi.productId = :productId AND pi.deleted = false")
    int countActiveImagesByProductId(@Param("productId") Long productId);

    /**
     * 상품의 특정 타입 이미지 개수 조회
     */
    @Query("SELECT COUNT(pi) FROM ProductImageEntity pi WHERE pi.productId = :productId AND pi.deleted = false " +
            "AND pi.imageType = :imageType")
    int countActiveImagesByProductIdAndType(@Param("productId") Long productId, @Param("imageType") ImageType imageType);

    /**
     * 특정 상품의 모든 썸네일 해제 (새 썸네일 설정 전 기존 것들 해제용)
     */
    @Modifying
    @Query("UPDATE ProductImageEntity pi SET pi.isThumbnail = false WHERE pi.productId = :productId")
    void unsetAllThumbnailsForProduct(@Param("productId") Long productId);

    /**
     * 상품 ID와 이미지 ID로 조회 (권한 체크용)
     */
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.imageId = :imageId AND pi.productId = :productId AND pi.deleted = false")
    Optional<ProductImageEntity> findByImageIdAndProductId(@Param("imageId") Long imageId, @Param("productId") Long productId);

    /**
     * 상품의 마지막 정렬 순서 조회 (새 이미지 추가 시 순서 결정용)
     */
    @Query("SELECT COALESCE(MAX(pi.sortOrder), 0) FROM ProductImageEntity pi WHERE pi.productId = :productId " +
            "AND pi.deleted = false AND pi.imageType = :imageType")
    int findMaxSortOrderByProductIdAndType(@Param("productId") Long productId, @Param("imageType") ImageType imageType);

    /**
     * 여러 상품의 썸네일 이미지들 일괄 조회 (상품 목록 페이지용)
     */
    @Query("SELECT pi FROM ProductImageEntity pi WHERE pi.productId IN :productIds AND pi.deleted = false " +
            "AND pi.imageType = 'MAIN' AND pi.isThumbnail = true")
    List<ProductImageEntity> findThumbnailsByProductIds(@Param("productIds") List<Long> productIds);
}