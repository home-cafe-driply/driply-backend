package com.driply.backend.domains.member.seller.service;

import com.driply.backend.domains.member.seller.dto.SellerUserDetails;
import com.driply.backend.domains.member.seller.entity.SellerEntity;
import com.driply.backend.domains.member.seller.repository.SellerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SellerUserDetailsService implements UserDetailsService {

    private final SellerRepository sellerRepository;

    public SellerUserDetailsService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.info("판매자 인증 조회: {}", email);

        // DB에서 조회 (username 대신 email 사용)
        SellerEntity sellerData = sellerRepository.findByEmail(email)
                .orElse(null);

        if (sellerData != null) {
            log.info("판매자 찾음: {} (회사명: {}, 인증상태: {})",
                    email, sellerData.getCompanyName(), sellerData.isVerified());
            // UserDetails에 담아서 return하면 AuthenticationManager가 검증 함
            return new SellerUserDetails(sellerData);
        }

        log.warn("판매자를 찾을 수 없음: {}", email);
        return null;
    }
}