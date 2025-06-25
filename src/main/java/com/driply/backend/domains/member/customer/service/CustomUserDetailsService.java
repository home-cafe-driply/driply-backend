package com.driply.backend.domains.member.customer.service;

import com.driply.backend.domains.member.customer.dto.CustomUserDetails;
import com.driply.backend.domains.member.customer.entity.CustomerEntity;
import com.driply.backend.domains.member.customer.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.info("사용자 인증 조회: {}", email);

        // DB에서 조회 (username 대신 email 사용)
        CustomerEntity customerData = customerRepository.findByEmail(email)
                .orElse(null);

        if (customerData != null) {
            log.info("사용자 찾음: {}", email);
            // UserDetails에 담아서 return하면 AuthenticationManager가 검증 함
            return new CustomUserDetails(customerData);
        }

        log.warn("사용자를 찾을 수 없음: {}", email);
        return null;
    }
}
