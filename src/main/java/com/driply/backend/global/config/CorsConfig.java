package com.driply.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

@Configuration
public class CorsConfig {

    /**
     * Spring Security용 CORS 설정
     * JWT 인증이 필요한 요청에서 사용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처 (프론트엔드 주소)
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Collections.singletonList("*"));

        // 인증 정보 포함 허용 (JWT 토큰 등)
        configuration.setAllowCredentials(true);

        // 허용할 헤더
        configuration.setAllowedHeaders(Collections.singletonList("*"));

        // Preflight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        // 프론트엔드에서 읽을 수 있는 응답 헤더 (JWT 토큰)
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Spring MVC용 CORS 설정
     * Preflight OPTIONS 요청 처리용
     */
    @Bean
    public WebMvcConfigurer corsWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {
                corsRegistry.addMapping("/**")
                        // 허용할 출처
                        .allowedOrigins("http://localhost:3000")

                        // 허용할 HTTP 메서드 (명시적 지정)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

                        // 허용할 헤더
                        .allowedHeaders("*")

                        // 인증 정보 포함 허용
                        .allowCredentials(true)

                        // Preflight 캐시 시간
                        .maxAge(3600);
            }
        };
    }
}
