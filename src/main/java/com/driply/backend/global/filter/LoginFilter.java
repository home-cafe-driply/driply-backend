package com.driply.backend.global.filter;

import com.driply.backend.domains.member.customer.dto.CustomUserDetails;
import com.driply.backend.global.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;

        setUsernameParameter("email");
        setPasswordParameter("password");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        // 클라이언트 요청에서 email, password 추출
        String email = obtainUsername(request); // email을 username 필드로
        String password = obtainPassword(request);

        log.info("로그인 시도: {}", email);

        // 스프링 시큐리티에서 email과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);

        // token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공시 JWT를 발급
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        log.info("로그인 성공");

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Long customerId = customUserDetails.getCustomerId();
        String email = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createAccessToken(customerId, email, role);
        String refreshToken = jwtUtil.createRefreshToken(customerId, email, role);

        // Access Token: Authorization 헤더
        response.setHeader("Authorization", "Bearer " + accessToken);

        // Refresh Token: HttpOnly 쿠키
        response.addCookie(createCookie(refreshToken, jwtUtil.getRefreshExpirationMs()));

        response.setStatus(HttpStatus.OK.value());

        log.info("JWT 토큰 발급 완료: customerId={}, email={}, role={}", customerId, email, role);
        log.info("Access Token 만료: {}분, Refresh Token 만료: {}시간",
                jwtUtil.getAccessExpirationMs() / (1000 * 60),
                jwtUtil.getRefreshExpirationMs() / (1000 * 60 * 60));
    }

    private Cookie createCookie(String value, Long expireMs) {
        Cookie cookie = new Cookie("refresh", value);

        //(밀리초 → 초 변환)
        int maxAgeSec = (int) (expireMs / 1000);
        cookie.setMaxAge(maxAgeSec);

        cookie.setHttpOnly(true);       // JavaScript 접근 차단
        // cookie.setSecure(true);      // HTTPS
        // cookie.setPath("/");         // 쿠키 유효 경로

        log.info("Refresh Token 쿠키 생성: key={}, maxAge={}초 ({}시간), httpOnly=true",
                "refresh", maxAgeSec, maxAgeSec / 3600);
        return cookie;
    }

    // 로그인 실패시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        log.warn("로그인 실패: {}", failed.getMessage());

        // 실패 응답 처리
        response.setStatus(401);
    }
}
