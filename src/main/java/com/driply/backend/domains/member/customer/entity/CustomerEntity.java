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

    /**
     * 비밀번호를 변경합니다.
     * 서비스 계층에서 이미 암호화된 비밀번호를 인자로 받아 업데이트합니다.
     *
     * @param newPassword 새로운 비밀번호(이미 암호화된 형태)
     * @return 업데이트된 고객 엔티티
     */
    public CustomerEntity changePassword(String newPassword) {
        return this.toBuilder()
                .password(newPassword)
                .build();
    }

    /**
     * 인증된 전화번호와 실명을 업데이트합니다.
     * 본인 인증 과정에서 확인된 정보를 저장할 때 사용됩니다.
     *
     * @param verifiedPhone 인증된 전화번호
     * @param verifiedName 인증된 실명
     * @return 업데이트된 고객 엔티티
     */
    public CustomerEntity updateVerifiedPhoneAndName(String verifiedPhone, String verifiedName) {
        return this.toBuilder()
                .phone(verifiedPhone)
                .name(verifiedName)
                .build();
    }

    /**
     * 닉네임을 업데이트합니다.
     * 프로필 수정 등에서 닉네임 변경 시 사용됩니다.
     *
     * @param nickname 새로운 닉네임
     * @return 업데이트된 고객 엔티티
     */
    public CustomerEntity updateNickname(String nickname) {
        return this.toBuilder()
                .nickname(nickname)
                .build();
    }

    /**
     * 이메일을 업데이트합니다.
     * 계정 설정 변경 시 사용됩니다.
     *
     * @param email 새로운 이메일
     * @return 업데이트된 고객 엔티티
     */
    public CustomerEntity updateEmail(String email) {
        return this.toBuilder()
                .email(email)
                .build();
    }

    /**
     * 인증 상태를 인증됨으로 변경합니다.
     * 본인 인증이 완료되었을 때 호출됩니다.
     *
     * @return 업데이트된 고객 엔티티
     */
    public CustomerEntity markAsPassed() {
        return this.toBuilder()
                .isPassed(true)
                .build();
    }

    /**
     * 인증 상태를 인증되지 않음으로 변경합니다.
     * 인증 취소 또는 재인증이 필요한 경우 호출됩니다.
     *
     * @return 업데이트된 고객 엔티티
     */
    public CustomerEntity unmarkAsPassed() {
        return this.toBuilder()
                .isPassed(false)
                .build();
    }
}
