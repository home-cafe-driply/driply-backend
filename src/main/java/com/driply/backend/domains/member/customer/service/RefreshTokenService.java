package com.driply.backend.domains.member.customer.service;

import com.driply.backend.domains.member.customer.entity.RefreshTokenEntity;
import com.driply.backend.domains.member.customer.repository.RefreshTokenRepository;
import com.driply.backend.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;

    /**
     * Refresh Token을 DB에 저장
     */
    public void saveRefreshToken(Long customerId, String refreshToken, Long expiredMs) {
        Date expireDate = new Date(System.currentTimeMillis() + expiredMs);
        String expiration = expireDate.toString();

        RefreshTokenEntity refreshEntity = new RefreshTokenEntity();
        refreshEntity.setCustomerId(customerId);
        refreshEntity.setRefresh(refreshToken);
        refreshEntity.setExpiration(expiration);

        refreshTokenRepository.save(refreshEntity);

        log.info("Refresh Token DB 저장 완료: customerId={}", customerId);
    }

    /**
     * Refresh Token이 DB에 존재하는지 확인
     */
    public boolean isValidRefreshToken(String refreshToken) {
        boolean exists = refreshTokenRepository.existsByRefresh(refreshToken);
        log.debug("Refresh Token DB 검증: token exists={}", exists);
        return exists;
    }

    /**
     * Refresh Token을 DB에서 삭제
     */
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByRefresh(refreshToken);
        log.info("Refresh Token DB 삭제 완료: token={}", refreshToken.substring(0, 20) + "...");
    }

    /**
     * Refresh Token 갱신 (Rotation 방식)
     * 기존 토큰 삭제 후 새 토큰 저장
     */
    public void renewRefreshToken(String oldRefreshToken, Long customerId, String email, String role) {
        // 1. 기존 토큰 삭제
        deleteRefreshToken(oldRefreshToken);

        // 2. 새 토큰 생성 및 저장
        String newRefreshToken = jwtUtil.createRefreshToken(customerId, email, role);
        saveRefreshToken(customerId, newRefreshToken, jwtUtil.getRefreshExpirationMs());

        log.info("Refresh Token 갱신 완료 (Rotation): customerId={}", customerId);
    }
}