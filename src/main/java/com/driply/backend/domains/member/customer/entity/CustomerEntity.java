package com.driply.backend.domains.member.customer.entity;

import com.driply.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Table(name = "customers")
@ToString(callSuper = true, of = {"customerId", "name", "userId", "email", "nickname"})
public class CustomerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed = false;

    // 비밀번호 변경
    public CustomerEntity changePassword(String newPassword) {
        return this.toBuilder()
                .password(newPassword)
                .build();
    }

    // Customer 엔티티에 추가할 메서드
    public CustomerEntity updateVerifiedPhoneAndName(String verifiedPhone, String verifiedName) {
        return this.toBuilder()
                .phone(verifiedPhone)
                .name(verifiedName)
                .build();
    }

    // 닉네임만 업데이트
    public CustomerEntity updateNickname(String nickname) {
        return this.toBuilder()
                .nickname(nickname)
                .build();
    }

    // 이메일 변경
    public CustomerEntity updateEmail(String email) {
        return this.toBuilder()
                .email(email)
                .build();
    }

    // 인증 상태 변경 - 인증됨으로
    public CustomerEntity markAsPassed() {
        return this.toBuilder()
                .isPassed(true)
                .build();
    }

    // 인증 상태 취소 - 인증되지 않음으로
    public CustomerEntity unmarkAsPassed() {
        return this.toBuilder()
                .isPassed(false)
                .build();
    }
}
