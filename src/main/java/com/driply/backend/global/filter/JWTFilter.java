package com.driply.backend.global.filter;

import com.driply.backend.domains.member.customer.dto.CustomUserDetails;
import com.driply.backend.domains.member.customer.entity.CustomerEntity;
import com.driply.backend.global.util.JWTUtil;
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
            return; // 조건이 해당되면 메소드 종료 (필수)
        }

        log.debug("JWT 토큰 검증 시작");

        // Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        // 토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            log.warn("JWT 토큰이 만료됨");
            filterChain.doFilter(request, response);
            return; // 조건이 해당되면 메소드 종료 (필수)
        }

        // 토큰에서 email과 role 획득
        Long customerId = jwtUtil.getCustomerId(token);
        String email = jwtUtil.getEmail(token);  // getUsername() 대신 getEmail() 사용
        String role = jwtUtil.getRole(token);

        log.info("JWT 토큰 검증 성공: customerId={}, email={}, role={}", customerId, email, role);

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
