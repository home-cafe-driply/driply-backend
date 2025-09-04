package com.driply.backend.global.config;

import com.driply.backend.domains.member.customer.repository.RefreshTokenRepository;
import com.driply.backend.domains.member.customer.service.CustomUserDetailsService;
import com.driply.backend.domains.member.seller.service.SellerRefreshTokenService;
import com.driply.backend.domains.member.seller.service.SellerUserDetailsService;
import com.driply.backend.global.filter.CustomLogoutFilter;
import com.driply.backend.global.filter.JWTFilter;
import com.driply.backend.global.filter.LoginFilter;
import com.driply.backend.global.filter.SellerLoginFilter;
import com.driply.backend.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final CorsConfigurationSource corsConfigurationSource;
    private final RefreshTokenRepository refreshTokenRepository;

    // Customer 관련 서비스
    private final CustomUserDetailsService customUserDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // Seller 관련 서비스
    private final SellerUserDetailsService sellerUserDetailsService;
    private final SellerRefreshTokenService sellerRefreshTokenService;

    /**
     * Customer 전용 AuthenticationManager (기본)
     */
    @Bean("customerAuthManager")
    @Primary
    public AuthenticationManager customerAuthenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder);

        return new ProviderManager(provider);
    }

    /**
     * Seller 전용 AuthenticationManager
     */
    @Bean("sellerAuthManager")
    public AuthenticationManager sellerAuthenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(sellerUserDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // CSRF 비활성화 (JWT 사용시 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // URL별 접근 권한 설정
                .authorizeHttpRequests((auth) -> auth
                        // 인증 불필요 경로 (통일된 API 경로)
                        .requestMatchers("/api/customer/login", "/", "/join").permitAll()
                        .requestMatchers("/api/seller/login", "/api/seller/join").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/logout").permitAll()
                        .requestMatchers("/test", "/profile").permitAll()

                        // 관리자 페이지 접근 거부
                        .requestMatchers("/admin").denyAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshTokenRepository), LogoutFilter.class)

                // Customer 로그인 필터 (기존)
                .addFilterAt(new LoginFilter(customerAuthenticationManager(), jwtUtil, refreshTokenRepository),
                        UsernamePasswordAuthenticationFilter.class)

                // Seller 로그인 필터 (새로 추가)
                .addFilterAt(new SellerLoginFilter(sellerAuthenticationManager(), jwtUtil, sellerRefreshTokenService),
                        UsernamePasswordAuthenticationFilter.class)

                .addFilterAfter(new JWTFilter(jwtUtil), LoginFilter.class)

                // 세션 사용 안함 (Stateless)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}