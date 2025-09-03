package com.driply.backend.domains.product.repository;

import com.driply.backend.domains.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * 활성 상품만 조회
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.deleted = false AND p.status = 'ACTIVE'")
    Page<ProductEntity> findActiveProducts(Pageable pageable);

    /**
     * 활성 상품 중 카테고리별 조회
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.deleted = false AND p.status = 'ACTIVE' AND p.category = :category")
    Page<ProductEntity> findActiveProductsByCategory(@Param("category") String category, Pageable pageable);

    /**
     * 활성 상품 중 검색
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.deleted = false AND p.status = 'ACTIVE' " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ProductEntity> searchActiveProducts(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 상품 상세 조회 (소비자용)
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.productId = :productId AND p.deleted = false AND p.status = 'ACTIVE'")
    Optional<ProductEntity> findActiveProductById(@Param("productId") Long productId);

    /**
     * 판매자별 상품 조회 (판매자용)
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.sellerId = :sellerId AND p.deleted = false")
    Page<ProductEntity> findProductsBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    /**
     * 판매자의 특정 상품 조회 (권한 체크용)
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.productId = :productId AND p.sellerId = :sellerId AND p.deleted = false")
    Optional<ProductEntity> findProductByIdAndSellerId(@Param("productId") Long productId, @Param("sellerId") Long sellerId);
}