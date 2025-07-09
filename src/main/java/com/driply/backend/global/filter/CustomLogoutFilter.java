package com.driply.backend.global.filter;

import com.driply.backend.domains.member.customer.repository.RefreshTokenRepository;
import com.driply.backend.global.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public CustomLogoutFilter(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // 1. POST /logout 요청인지 확인
        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("로그아웃 요청 처리 시작");

        // 2. 쿠키에서 refresh token 추출
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }

        // 3. refresh token null check
        if (refresh == null) {
            log.warn("로그아웃 요청에 refresh token이 없음");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 4. JWT 만료 여부 확인
        boolean isExpired = false;
        try {
            if (jwtUtil.isExpired(refresh)) {
                log.info("만료된 refresh token이지만 로그아웃 허용");
                isExpired = true;
            }
        } catch (ExpiredJwtException e) {
            log.info("JWT 만료 예외이지만 로그아웃 허용: {}", e.getMessage());
            isExpired = true;
        } catch (Exception e) {
            log.warn("JWT 파싱 실패로 로그아웃 차단: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 5. 토큰이 refresh인지 확인
        if (!isExpired) {
            try {
                String category = jwtUtil.getCategory(refresh);
                if (!category.equals("refresh")) {
                    log.warn("유효하지 않은 토큰 타입으로 로그아웃 요청: {}", category);
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
            } catch (Exception e) {
                log.warn("토큰 카테고리 확인 실패: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        // 6. DB에 토큰이 저장되어 있는지 확인
        if (!isExpired) {
            Boolean isExist = refreshTokenRepository.existsByRefresh(refresh);
            if (!isExist) {
                log.warn("DB에 존재하지 않는 refresh token으로 로그아웃 요청");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        // 7. 로그아웃 진행
        // 7-1. Refresh 토큰 DB에서 제거
        if (!isExpired) {
            refreshTokenRepository.deleteByRefresh(refresh);
            log.info("DB에서 refresh token 삭제 완료");
        } else {
            log.info("만료된 토큰이므로 DB 삭제 생략");
        }

        // 7-2. Refresh 토큰 쿠키 초기화
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        log.info("로그아웃 처리 완료");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
