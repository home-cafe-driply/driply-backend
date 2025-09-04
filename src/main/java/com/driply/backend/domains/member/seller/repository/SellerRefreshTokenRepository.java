package com.driply.backend.domains.member.seller.repository;

import com.driply.backend.domains.member.seller.entity.SellerRefreshTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRefreshTokenRepository extends JpaRepository<SellerRefreshTokenEntity, Long> {

    /**
     * Seller Refresh Token 존재 여부 확인 (토큰 갱신 시 검증용)
     */
    Boolean existsByRefresh(String refresh);

    /**
     * Seller Refresh Token으로 삭제 (로그아웃 시 해당 토큰만 무효화)
     */
    @Transactional
    void deleteByRefresh(String refresh);
}