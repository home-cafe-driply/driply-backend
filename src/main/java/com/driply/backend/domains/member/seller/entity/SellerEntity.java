package com.driply.backend.domains.member.seller.entity;

import com.driply.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Table(name = "sellers")
@ToString(callSuper = true, of = {"sellerId", "email", "companyName"})
public class SellerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "business_number", unique = true, length = 15)
    private String businessNumber;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    /**
     * JWT subject용 식별자
     */
    public String getSubject() {
        return String.valueOf(sellerId);
    }
}
