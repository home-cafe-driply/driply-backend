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

    public Long getCustomerId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("customerId", Long.class);
    }

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

    // Access Token
    public String createAccessToken(Long customerId, String email, String role) {
        return createJwt("access", customerId, email, role, accessExpirationMs);
    }

    // Refresh Token
    public String createRefreshToken(Long customerId, String email, String role) {
        return createJwt("refresh", customerId, email, role, refreshExpirationMs);
    }

    public String createJwt(String category, Long customerId, String email, String role, Long expiredMs) {
        log.info("JWT 토큰 생성: category={}, customerId={}, email={}, role={}, expiredMs={}ms",
                category, customerId, email, role, expiredMs);

        return Jwts.builder()
                .claim("category", category)        // 토큰 타입
                .claim("customerId", customerId)    // 고객 ID
                .claim("email", email)              // 이메일
                .claim("role", role)                // 권한
                .subject(String.valueOf(customerId))    // JWT 표준 subject
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

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
}
