package com.driply.backend.domains.member.customer.controller;

import com.driply.backend.domains.member.customer.service.RefreshTokenService;
import com.driply.backend.global.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * Access Token 갱신 API
     * Refresh Token(쿠키)을 받아서 새로운 Access Token을 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {

        log.info("Access Token 갱신 요청");

        // 1. 쿠키에서 Refresh Token 추출
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 🆕 추가: 쿠키 값 로그
        log.info("쿠키에서 추출한 refresh token: {}", refreshToken != null ? refreshToken.substring(0, 20) + "..." : "null");

        // 🆕 추가: DB 존재 여부 확인 전에 직접 로그
        if (refreshToken != null) {
            boolean exists = refreshTokenService.isValidRefreshToken(refreshToken);
            log.info("DB에서 토큰 존재 여부: {}", exists);
        }

        if (refreshToken == null) {
            log.warn("Refresh Token이 쿠키에 없음");
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        // 2. Refresh Token 만료 여부 확인
        try {
            if (jwtUtil.isExpired(refreshToken)) {
                log.warn("Refresh Token이 만료됨");
                return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
            }
        } catch (ExpiredJwtException e) {
            log.warn("Refresh Token 만료 예외: {}", e.getMessage());
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.warn("Refresh Token 검증 중 오류: {}", e.getMessage());
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 3. Refresh Token 타입 검증
        try {
            String category = jwtUtil.getCategory(refreshToken);
            if (!"refresh".equals(category)) {
                log.warn("유효하지 않은 토큰 타입: {}", category);
                return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.warn("토큰 타입 검증 실패: {}", e.getMessage());
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 4. 🆕 DB에서 Refresh Token 존재 여부 확인
        if (!refreshTokenService.isValidRefreshToken(refreshToken)) {
            log.warn("DB에 존재하지 않는 Refresh Token");
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 5. JWT에서 사용자 정보 추출
        Long customerId;
        String email;
        String role;

        try {
            customerId = jwtUtil.getCustomerId(refreshToken);
            email = jwtUtil.getEmail(refreshToken);
            role = jwtUtil.getRole(refreshToken);

            log.info("Refresh Token에서 사용자 정보 추출: customerId={}, email={}, role={}",
                    customerId, email, role);
        } catch (Exception e) {
            log.warn("사용자 정보 추출 실패: {}", e.getMessage());
            return new ResponseEntity<>("invalid refresh token payload", HttpStatus.BAD_REQUEST);
        }

        // 6. 새로운 Access Token 생성
        String newAccessToken = jwtUtil.createAccessToken(customerId, email, role);

        // 7. 🆕 Refresh Token 갱신 (Rotation 방식으로 DB에서 처리)
        String newRefreshToken = refreshTokenService.renewRefreshToken(refreshToken, customerId, email, role);

        // 8. 응답 헤더 설정
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(createCookie("refresh", newRefreshToken, jwtUtil.getRefreshExpirationMs()));

        log.info("토큰 갱신 완료 (Rotation): customerId={}, Access Token={}분, Refresh Token={}시간",
                customerId,
                jwtUtil.getAccessExpirationMs() / (1000 * 60),
                jwtUtil.getRefreshExpirationMs() / (1000 * 60 * 60));

        return new ResponseEntity<>("tokens renewed with rotation", HttpStatus.OK);
    }


    private Cookie createCookie(String key, String value, Long expireMs) {
        Cookie cookie = new Cookie(key, value);

        // JWT 토큰과 동일한 만료시간으로 설정 (밀리초 → 초 변환)
        int maxAgeSec = (int) (expireMs / 1000);
        cookie.setMaxAge(maxAgeSec);

        cookie.setHttpOnly(true);       // JavaScript 접근 차단 (XSS 방어)
        // cookie.setSecure(true);      // HTTPS에서만 전송 (운영환경에서 활성화)
        // cookie.setPath("/");         // 쿠키 유효 경로 (기본값 사용)

        log.debug("새로운 Refresh Token 쿠키 생성: key={}, maxAge={}초 ({}시간), httpOnly=true",
                key, maxAgeSec, maxAgeSec / 3600);
        return cookie;
    }

}