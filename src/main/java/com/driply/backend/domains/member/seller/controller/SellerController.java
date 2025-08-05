package com.driply.backend.domains.member.seller.controller;

import com.driply.backend.domains.member.seller.dto.SellerJoinDTO;
import com.driply.backend.domains.member.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/join")
    public ResponseEntity<?> createSeller(@RequestBody SellerJoinDTO dto) {
        Long sellerId = sellerService.joinProcess(dto);  // DB에 바로 저장

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "success", true,
                        "message", "판매자 가입이 완료되었습니다.",
                        "sellerId", sellerId
                ));
    }
}
