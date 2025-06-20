package com.driply.backend.domains.member.customer.repository;

import com.driply.backend.domains.member.customer.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    /**
     * 이메일로 고객 조회 (로그인용)
     */
    Optional<CustomerEntity> findByEmail(String email);

    /**
     * 이메일 중복 체크 (회원가입용)
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 중복 체크 (회원가입용)
     */
    boolean existsByNickname(String nickname);
}
