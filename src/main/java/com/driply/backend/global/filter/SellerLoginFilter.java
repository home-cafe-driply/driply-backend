package com.driply.backend.global.filter;

import com.driply.backend.domains.member.seller.dto.SellerUserDetails;
import com.driply.backend.domains.member.seller.service.SellerRefreshTokenService;
import com.driply.backend.global.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.Iterator;

@Slf4j
public class SellerLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final SellerRefreshTokenService sellerRefreshTokenService;

    public SellerLoginFilter(AuthenticationManager authenticationManager,
                             JWTUtil jwtUtil,
                             SellerRefreshTokenService sellerRefreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.sellerRefreshTokenService = sellerRefreshTokenService;

        // Seller 전용 로그인 경로 설정
        setFilterProcessesUrl("/api/seller/login");
        setUsernameParameter("email");
        setPasswordParameter("password");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        log.info("판매자 로그인 요청 시작 - Content-Type: {}", request.getContentType());

        // JSON 요청 처리
        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            try {
                // JSON 데이터 읽기
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }

                String json = sb.toString();
                log.info("수신된 JSON 데이터: {}", json);

                // 간단한 JSON 파싱 (ObjectMapper 없이)
                String email = extractJsonValue(json, "email");
                String password = extractJsonValue(json, "password");

                log.info("파싱된 email: {}, password 길이: {}", email, password != null ? password.length() : "null");

                if (email == null || password == null) {
                    throw new AuthenticationServiceException("이메일 또는 비밀번호가 누락되었습니다.");
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);
                return authenticationManager.authenticate(authToken);

            } catch (Exception e) {
                log.error("JSON 파싱 오류: {}", e.getMessage(), e);
                throw new AuthenticationServiceException("요청 파싱 실패", e);
            }
        }

        // 기존 form-data 처리 (백업)
        String email = obtainUsername(request);
        String password = obtainPassword(request);

        log.info("Form 데이터로 파싱된 email: {}", email);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);
        return authenticationManager.authenticate(authToken);
    }

    /**
     * 간단한 JSON 값 추출 (ObjectMapper 없이)
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex);
        int startQuote = json.indexOf("\"", colonIndex);
        int endQuote = json.indexOf("\"", startQuote + 1);

        if (startQuote == -1 || endQuote == -1) return null;

        return json.substring(startQuote + 1, endQuote);
    }

    // 로그인 성공시 JWT를 발급
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        log.info("판매자 로그인 성공");

        SellerUserDetails sellerUserDetails = (SellerUserDetails) authentication.getPrincipal();

        Long sellerId = sellerUserDetails.getSellerId();
        String email = sellerUserDetails.getUsername();
        String companyName = sellerUserDetails.getCompanyName();
        boolean isVerified = sellerUserDetails.isVerified();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createSellerAccessToken(sellerId, email, role);
        String refreshToken = jwtUtil.createSellerRefreshToken(sellerId, email, role);

        // Access Token: Authorization 헤더
        response.setHeader("Authorization", "Bearer " + accessToken);

        // Refresh Token: HttpOnly 쿠키 (seller 전용)
        response.addCookie(createCookie("seller_refresh", refreshToken, jwtUtil.getRefreshExpirationMs()));

        response.setStatus(HttpStatus.OK.value());

        log.info("판매자 JWT 토큰 발급 완료: sellerId={}, email={}, companyName={}, role={}, verified={}",
                sellerId, email, companyName, role, isVerified);
        log.info("Access Token 만료: {}분, Refresh Token 만료: {}시간",
                jwtUtil.getAccessExpirationMs() / (1000 * 60),
                jwtUtil.getRefreshExpirationMs() / (1000 * 60 * 60));

        // Seller RefreshToken DB 저장
        sellerRefreshTokenService.saveRefreshToken(sellerId, refreshToken, jwtUtil.getRefreshExpirationMs());
    }

    private Cookie createCookie(String name, String value, Long expireMs) {
        Cookie cookie = new Cookie(name, value);

        //(밀리초 → 초 변환)
        int maxAgeSec = (int) (expireMs / 1000);
        cookie.setMaxAge(maxAgeSec);

        cookie.setHttpOnly(true);       // JavaScript 접근 차단
        // cookie.setSecure(true);      // HTTPS
        // cookie.setPath("/");         // 쿠키 유효 경로

        log.info("Seller Refresh Token 쿠키 생성: key={}, maxAge={}초 ({}시간), httpOnly=true",
                name, maxAgeSec, maxAgeSec / 3600);
        return cookie;
    }

    // 로그인 실패시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        log.warn("판매자 로그인 실패: {}", failed.getMessage());

        // 실패 응답 처리
        response.setStatus(401);
    }
}