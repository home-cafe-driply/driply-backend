package com.driply.backend.domains.product;

import com.driply.backend.domains.member.seller.entity.SellerEntity;
import com.driply.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Table(name = "products")
@ToString(callSuper = true, of = {"productId", "name", "basePrice", "stockQuantity", "status"})
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "stock_quantity")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "display_state")
    @Builder.Default
    private Integer displayState = 1; // 1: 전시중

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 메타데이터 (JSONB) - 옵션, 변형, 라벨 등 저장
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    // ========== 연관관계 ========== //

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
//    private SellerEntity seller;

    // ========== 비즈니스 메서드 ========== //

    /**
     * 재고 감소
     */
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
    }

    /**
     * 판매 가능 여부 확인
     */
    public boolean isAvailable() {
        return "ACTIVE".equals(this.status) && this.stockQuantity > 0 && this.isActive();
    }
}
