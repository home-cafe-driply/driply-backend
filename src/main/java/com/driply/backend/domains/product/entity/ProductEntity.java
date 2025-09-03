package com.driply.backend.domains.product.entity;

import com.driply.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Table(name = "products")
@ToString(callSuper = true, of = {"productId", "name", "basePrice", "stockQuantity"})
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "seller_id", nullable = false)
    @Builder.Default
    private Long sellerId = 1L; // 일단 고정

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * 재고 감소
     */
    public void decreaseStock(int quantity) {
        this.stockQuantity = Math.max(0, this.stockQuantity - quantity);
    }

    /**
     * 재고 증가
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * 판매 가능 여부 확인
     */
    public boolean isAvailable() {
        return this.status.isActive() &&
                this.stockQuantity > 0 &&
                this.isActive(); // BaseEntity의 논리삭제 확인
    }
}
