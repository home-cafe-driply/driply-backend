package com.driply.backend.domains.member.customer.controller;

import com.driply.backend.domains.member.customer.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MainController {
    @GetMapping("/")
    public String mainPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "Driply Backend - 인증되지 않은 사용자";
        }

        String currentUser = authentication.getName();
        log.info("메인 페이지 접근: {}", currentUser);

        return "Driply Backend - 현재 사용자: " + currentUser;
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long customerId = userDetails.getCustomerId();
        String email = authentication.getName();
        String role = authentication.getAuthorities().toString();

        log.info("프로필 페이지 접근: customerId={}, email={}, role={}", customerId, email, role);

        return String.format("프로필 페이지 - ID: %d, 사용자: %s, 권한: %s", customerId, email, role);
    }
}