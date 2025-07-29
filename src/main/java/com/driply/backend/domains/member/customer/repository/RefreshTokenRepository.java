package com.driply.backend.domains.member.customer.repository;

import com.driply.backend.domains.member.customer.entity.RefreshTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    /**
     * Refresh Token 존재 여부 확인 (토큰 갱신 시 검증용)
     */
    Boolean existsByRefresh(String refresh);

    /**
     * Refresh Token으로 삭제 (로그아웃 시 해당 토큰만 무효화)
     */
    @Transactional
    void deleteByRefresh(String refresh);
}
