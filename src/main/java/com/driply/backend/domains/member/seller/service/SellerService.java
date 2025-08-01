package com.driply.backend.domains.member.seller.service;

import com.driply.backend.domains.member.seller.dto.SellerJoinDTO;
import com.driply.backend.domains.member.seller.entity.SellerEntity;
import com.driply.backend.domains.member.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 판매자 회원가입 (테스트용)
     */
    @Transactional
    public Long joinProcess(SellerJoinDTO sellerJoinDTO) {
        // 중복 체크
        if (sellerRepository.existsByEmail(sellerJoinDTO.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        if (sellerRepository.existsByBusinessNumber(sellerJoinDTO.getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다.");
        }

        // 판매자 생성
        SellerEntity seller = SellerEntity.builder()
                .email(sellerJoinDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(sellerJoinDTO.getPassword()))
                .companyName(sellerJoinDTO.getCompanyName())
                .businessNumber(sellerJoinDTO.getBusinessNumber())
                .isVerified(true) // 테스트용으로 바로 인증
                .build();

        SellerEntity savedSeller = sellerRepository.save(seller);

        return savedSeller.getSellerId();
    }

    /**
     * Product 개발용 - ID로 판매자 조회
     */
    public SellerEntity findById(Long sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다."));
    }
}
