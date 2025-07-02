package com.driply.backend.global.filter;

import com.driply.backend.domains.member.customer.dto.CustomUserDetails;
import com.driply.backend.domains.member.customer.entity.CustomerEntity;
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
        Long customerId;
        String email;
        String role;

        try {
            customerId = jwtUtil.getCustomerId(accessToken);
            email = jwtUtil.getEmail(accessToken);
            role = jwtUtil.getRole(accessToken);

            log.info("JWT 토큰 검증 성공: customerId={}, email={}, role={}", customerId, email, role);
        } catch (Exception e) {
            log.warn("토큰에서 사용자 정보 추출 실패: {}", e.getMessage());
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.print("invalid token payload");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // CustomerEntity를 생성하여 값 set
        CustomerEntity customerEntity = CustomerEntity.builder()
                .customerId(customerId)
                .email(email)
                .password("temp")  // 임시 비밀번호 (실제로는 사용되지 않음)
                .name("JWT_CUSTOMER")  // 임시 이름
                .nickname("JWT_CUSTOMER")  // 임시 닉네임
                .build();

        // UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(customerEntity);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());

        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("Spring Security 인증 컨텍스트에 사용자 등록 완료");

        filterChain.doFilter(request, response);
    }
}
