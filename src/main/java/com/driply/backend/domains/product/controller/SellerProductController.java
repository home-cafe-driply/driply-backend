package com.driply.backend.domains.product.controller;

import com.driply.backend.domains.member.seller.dto.SellerUserDetails;
import com.driply.backend.domains.product.dto.ProductRequestDTO;
import com.driply.backend.domains.product.dto.ProductResponseDTO;
import com.driply.backend.domains.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 판매자 상품 관리 API (JWT 인증 필요)
 * ROLE_SELLER 권한이 있는 사용자만 접근 가능
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/products")
public class SellerProductController {

    private final ProductService productService;

    /**
     * 내 상품 목록 조회
     * GET /api/seller/products
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getMyProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Long sellerId = getSellerIdFromAuth(authentication);

            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                    ? Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<ProductResponseDTO> products = productService.getProductsBySeller(sellerId, pageable);

            log.info("판매자 상품 목록 조회: sellerId={}, total={}", sellerId, products.getTotalElements());

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            log.error("판매자 상품 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 상품 등록
     * POST /api/seller/products
     */
    @PostMapping
    public ResponseEntity<?> createProduct(
            Authentication authentication,
            @RequestBody ProductRequestDTO dto) {

        try {
            Long sellerId = getSellerIdFromAuth(authentication);

            ProductResponseDTO product = productService.createProduct(sellerId, dto);

            log.info("상품 등록 성공: sellerId={}, productId={}, name={}",
                    sellerId, product.getProductId(), product.getName());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "상품이 등록되었습니다",
                            "product", product
                    ));

        } catch (IllegalArgumentException e) {
            log.warn("상품 등록 실패 - 입력값 오류: error={}", e.getMessage());

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            log.error("상품 등록 서버 오류", e);

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "서버 오류가 발생했습니다"
                    ));
        }
    }

    /**
     * 상품 수정
     * PUT /api/seller/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ProductRequestDTO dto) {

        try {
            Long sellerId = getSellerIdFromAuth(authentication);

            ProductResponseDTO product = productService.updateProduct(sellerId, id, dto);

            log.info("상품 수정 성공: sellerId={}, productId={}, name={}",
                    sellerId, id, product.getName());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상품이 수정되었습니다",
                    "product", product
            ));

        } catch (IllegalArgumentException e) {
            log.warn("상품 수정 실패 - 권한 없음 또는 존재하지 않음: productId={}, error={}", id, e.getMessage());

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            log.error("상품 수정 서버 오류: productId={}", id, e);

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "서버 오류가 발생했습니다"
                    ));
        }
    }

    /**
     * 상품 삭제
     * DELETE /api/seller/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            Authentication authentication,
            @PathVariable Long id) {

        try {
            Long sellerId = getSellerIdFromAuth(authentication);

            productService.deleteProduct(sellerId, id);

            log.info("상품 삭제 성공: sellerId={}, productId={}", sellerId, id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상품이 삭제되었습니다"
            ));

        } catch (IllegalArgumentException e) {
            log.warn("상품 삭제 실패 - 권한 없음 또는 존재하지 않음: productId={}, error={}", id, e.getMessage());

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            log.error("상품 삭제 서버 오류: productId={}", id, e);

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "서버 오류가 발생했습니다"
                    ));
        }
    }

    /**
     * Authentication에서 판매자 ID 추출
     */
    private Long getSellerIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다");
        }

        if (authentication.getPrincipal() instanceof SellerUserDetails) {
            SellerUserDetails sellerDetails = (SellerUserDetails) authentication.getPrincipal();
            return sellerDetails.getSellerId();
        }

        throw new IllegalArgumentException("판매자 권한이 없습니다");
    }
}