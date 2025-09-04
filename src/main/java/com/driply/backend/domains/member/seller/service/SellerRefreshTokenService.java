package com.driply.backend.domains.member.seller.service;

import com.driply.backend.domains.member.seller.entity.SellerRefreshTokenEntity;
import com.driply.backend.domains.member.seller.repository.SellerRefreshTokenRepository;
import com.driply.backend.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerRefreshTokenService {

    private final SellerRefreshTokenRepository sellerRefreshTokenRepository;
    private final JWTUtil jwtUtil;

    /**
     * Seller Refresh Token을 DB에 저장
     */
    public void saveRefreshToken(Long sellerId, String refreshToken, Long expiredMs) {
        Date expireDate = new Date(System.currentTimeMillis() + expiredMs);
        String expiration = expireDate.toString();

        SellerRefreshTokenEntity refreshEntity = new SellerRefreshTokenEntity();
        refreshEntity.setSellerId(sellerId);
        refreshEntity.setRefresh(refreshToken);
        refreshEntity.setExpiration(expiration);

        sellerRefreshTokenRepository.save(refreshEntity);

        log.info("Seller Refresh Token DB 저장 완료: sellerId={}", sellerId);
    }

    /**
     * Seller Refresh Token이 DB에 존재하는지 확인
     */
    public boolean isValidRefreshToken(String refreshToken) {
        boolean exists = sellerRefreshTokenRepository.existsByRefresh(refreshToken);
        log.debug("Seller Refresh Token DB 검증: token exists={}", exists);
        return exists;
    }

    /**
     * Seller Refresh Token을 DB에서 삭제
     */
    public void deleteRefreshToken(String refreshToken) {
        sellerRefreshTokenRepository.deleteByRefresh(refreshToken);
        log.info("Seller Refresh Token DB 삭제 완료: token={}", refreshToken.substring(0, 20) + "...");
    }

    /**
     * Seller Refresh Token 갱신 (Rotation 방식)
     * 기존 토큰 삭제 후 새 토큰 저장하고 반환
     */
    public String renewRefreshToken(String oldRefreshToken, Long sellerId, String email, String role) {
        // 1. 기존 토큰 삭제
        deleteRefreshToken(oldRefreshToken);

        // 2. 새 토큰 생성 및 저장
        String newRefreshToken = jwtUtil.createSellerRefreshToken(sellerId, email, role);
        saveRefreshToken(sellerId, newRefreshToken, jwtUtil.getRefreshExpirationMs());

        log.info("Seller Refresh Token 갱신 완료 (Rotation): sellerId={}", sellerId);

        return newRefreshToken;
    }
}