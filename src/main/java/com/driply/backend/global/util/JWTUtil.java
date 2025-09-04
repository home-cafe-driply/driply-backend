package com.driply.backend.global.util;

import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    private final SecretKey secretKey;
    @Getter
    private final Long accessExpirationMs;
    @Getter
    private final Long refreshExpirationMs;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret,
                   @Value("${spring.jwt.access-expiration:600000}") Long accessExpirationMs,
                   @Value("${spring.jwt.refresh-expiration:86400000}") Long refreshExpirationMs) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;

        log.info("JWT 유틸리티 초기화 완료 - Access Token: {}ms ({}분), Refresh Token: {}ms ({}시간)",
                accessExpirationMs, accessExpirationMs / (1000 * 60),
                refreshExpirationMs, refreshExpirationMs / (1000 * 60 * 60));
    }

    // ============= Customer 관련 메서드들 =============

    public Long getCustomerId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("customerId", Long.class);
    }

    // ============= Seller 관련 메서드들 =============

    public Long getSellerId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("sellerId", Long.class);
    }

    // ============= 공통 메서드들 =============

    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public String getCategory(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("category", String.class);
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("subject", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    // ============= Customer 토큰 생성 =============

    public String createCustomerAccessToken(Long customerId, String email, String role) {
        return createCustomerJwt("access", customerId, email, role, accessExpirationMs);
    }

    public String createCustomerRefreshToken(Long customerId, String email, String role) {
        return createCustomerJwt("refresh", customerId, email, role, refreshExpirationMs);
    }

    public String createCustomerJwt(String category, Long customerId, String email, String role, Long expiredMs) {
        log.info("Customer JWT 토큰 생성: category={}, customerId={}, email={}, role={}, expiredMs={}ms",
                category, customerId, email, role, expiredMs);

        return Jwts.builder()
                .claim("category", category)
                .claim("customerId", customerId)
                .claim("email", email)
                .claim("role", role)
                .subject(String.valueOf(customerId))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // ============= Seller 토큰 생성 =============

    public String createSellerAccessToken(Long sellerId, String email, String role) {
        return createSellerJwt("access", sellerId, email, role, accessExpirationMs);
    }

    public String createSellerRefreshToken(Long sellerId, String email, String role) {
        return createSellerJwt("refresh", sellerId, email, role, refreshExpirationMs);
    }

    public String createSellerJwt(String category, Long sellerId, String email, String role, Long expiredMs) {
        log.info("Seller JWT 토큰 생성: category={}, sellerId={}, email={}, role={}, expiredMs={}ms",
                category, sellerId, email, role, expiredMs);

        return Jwts.builder()
                .claim("category", category)
                .claim("sellerId", sellerId)
                .claim("email", email)
                .claim("role", role)
                .subject(String.valueOf(sellerId))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // ============= 기존 메서드들 (Customer 호환성) =============

    public String createAccessToken(Long customerId, String email, String role) {
        return createCustomerAccessToken(customerId, email, role);
    }

    public String createRefreshToken(Long customerId, String email, String role) {
        return createCustomerRefreshToken(customerId, email, role);
    }

    public String createJwt(String category, Long customerId, String email, String role, Long expiredMs) {
        return createCustomerJwt(category, customerId, email, role, expiredMs);
    }

    // ============= 검증 메서드들 =============

    public boolean isAccessToken(String token) {
        String category = getCategory(token);
        return "access".equals(category);
    }

    public boolean isRefreshToken(String token) {
        String category = getCategory(token);
        return "refresh".equals(category);
    }

    public boolean isValidAccessToken(String token) {
        try {
            return !isExpired(token) && isAccessToken(token);
        } catch (Exception e) {
            log.warn("Access Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public boolean isValidRefreshToken(String token) {
        try {
            return !isExpired(token) && isRefreshToken(token);
        } catch (Exception e) {
            log.warn("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // ============= 사용자 타입 구분 =============

    public boolean isCustomerToken(String token) {
        try {
            String role = getRole(token);
            return "ROLE_CUSTOMER".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSellerToken(String token) {
        try {
            String role = getRole(token);
            return "ROLE_SELLER".equals(role);
        } catch (Exception e) {
            return false;
        }
    }
}