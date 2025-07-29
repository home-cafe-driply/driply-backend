package com.driply.backend.domains.member.customer.entity;

import com.driply.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Table(name = "customers")
@ToString(callSuper = true, of = {"customerId", "email", "name", "nickname", "phone"})
public class CustomerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_passed", nullable = false)
    @Builder.Default
    private boolean isPassed = false;

    /**
     * JWT subject용 식별자 (customerId를 문자열로)
     */
    public String getSubject() {
        return String.valueOf(customerId);
    }
}
