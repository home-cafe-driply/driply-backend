package com.driply.backend.domains.member.customer.service;

import com.driply.backend.domains.member.customer.dto.CustomUserDetails;
import com.driply.backend.domains.member.customer.entity.CustomerEntity;
import com.driply.backend.domains.member.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.info("사용자 인증 조회: {}", email);

        CustomerEntity customerData = customerRepository.findByEmail(email)
                .orElse(null);

        if (customerData != null) {
            log.info("사용자 찾음: {}", email);
            return new CustomUserDetails(customerData);
        }

        log.warn("사용자를 찾을 수 없음: {}", email);
        return null;
    }
}
