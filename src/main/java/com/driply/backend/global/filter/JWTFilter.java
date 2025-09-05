package com.driply.backend.global.filter;

import com.driply.backend.domains.member.customer.dto.CustomUserDetails;
import com.driply.backend.domains.member.customer.entity.CustomerEntity;
import com.driply.backend.domains.member.seller.dto.SellerUserDetails;
import com.driply.backend.domains.member.seller.entity.SellerEntity;
import com.driply.backend.global.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.debug("JWT 토큰이 없음 - 인증되지 않은 요청");
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("JWT 토큰 검증 시작");

        // Bearer 제거
        String accessToken = authorization.split(" ")[1];

        // 토큰 만료 여부 확인
        try {
            if (jwtUtil.isExpired(accessToken)) {
                log.warn("JWT 토큰이 만료됨");
                response.setContentType("text/plain; charset=UTF-8");
                PrintWriter writer = response.getWriter();
                writer.print("access token expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰 만료 예외: {}", e.getMessage());
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception e) {
            log.warn("JWT 토큰 검증 중 오류: {}", e.getMessage());
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print("invalid token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰 타입 검증
        try {
            String category = jwtUtil.getCategory(accessToken);
            if (!"access".equals(category)) {
                log.warn("유효하지 않은 토큰 타입: {}", category);
                response.setContentType("text/plain; charset=UTF-8");
                PrintWriter writer = response.getWriter();
                writer.print("invalid access token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.warn("토큰 타입 검증 실패: {}", e.getMessage());
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰에서 사용자 정보 추출
        String email;
        String role;

        try {
            email = jwtUtil.getEmail(accessToken);
            role = jwtUtil.getRole(accessToken);

            log.info("JWT 토큰 검증 성공: email={}, role={}", email, role);
        } catch (Exception e) {
            log.warn("토큰에서 사용자 정보 추출 실패: {}", e.getMessage());
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print("invalid token payload");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 역할에 따라 UserDetails 생성
        UserDetails userDetails;

        if ("ROLE_CUSTOMER".equals(role)) {
            // Customer 처리
            Long customerId = jwtUtil.getCustomerId(accessToken);

            CustomerEntity customerEntity = CustomerEntity.builder()
                    .customerId(customerId)
                    .email(email)
                    .password("temp")
                    .name("JWT_CUSTOMER")
                    .nickname("JWT_CUSTOMER")
                    .build();

            userDetails = new CustomUserDetails(customerEntity);

        } else if ("ROLE_SELLER".equals(role)) {
            // Seller 처리
            Long sellerId = jwtUtil.getSellerId(accessToken);

            SellerEntity sellerEntity = SellerEntity.builder()
                    .sellerId(sellerId)
                    .email(email)
                    .password("temp")
                    .companyName("JWT_SELLER")
                    .businessNumber("000-00-00000")
                    .isVerified(true)
                    .build();

            userDetails = new SellerUserDetails(sellerEntity);

        } else {
            log.warn("지원하지 않는 역할: {}", role);
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print("unsupported role");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("Spring Security 인증 컨텍스트에 사용자 등록 완료: role={}", role);

        filterChain.doFilter(request, response);
    }
}