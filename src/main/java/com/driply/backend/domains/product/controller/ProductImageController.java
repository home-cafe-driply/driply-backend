package com.driply.backend.domains.product.controller;

import com.driply.backend.domains.member.seller.dto.SellerUserDetails;
import com.driply.backend.domains.product.dto.ProductImageDTO;
import com.driply.backend.domains.product.entity.ImageType;
import com.driply.backend.domains.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/seller/products")
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * 상품 이미지 업로드
     * POST /api/seller/products/{productId}/images
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<?> uploadProductImage(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "imageType", defaultValue = "MAIN") String imageType,
            @RequestParam(value = "isThumbnail", defaultValue = "false") boolean isThumbnail) {

        try {
            Long sellerId = getSellerIdFromAuth(authentication);

            // ImageType enum 변환
            ImageType type = ImageType.valueOf(imageType.toUpperCase());

            // 이미지 업로드 및 DB 저장
            ProductImageDTO imageDTO = productImageService.addProductImage(
                    sellerId, productId, file, type, isThumbnail);

            log.info("상품 이미지 업로드 성공: sellerId={}, productId={}, imageId={}",
                    sellerId, productId, imageDTO.getImageId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이미지 업로드 성공",
                    "image", imageDTO
            ));

        } catch (IllegalArgumentException e) {
            log.warn("이미지 업로드 실패 - 입력값 오류: productId={}, error={}", productId, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("이미지 업로드 서버 오류: productId={}", productId, e);

            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다"
            ));
        }
    }

    /**
     * 상품 이미지 삭제
     * DELETE /api/seller/products/images/{imageId}
     */
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(
            Authentication authentication,
            @PathVariable Long imageId) {

        try {
            Long sellerId = getSellerIdFromAuth(authentication);

            productImageService.deleteProductImage(sellerId, imageId);

            log.info("상품 이미지 삭제 성공: sellerId={}, imageId={}", sellerId, imageId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이미지 삭제 성공"
            ));

        } catch (IllegalArgumentException e) {
            log.warn("이미지 삭제 실패 - 권한 없음: imageId={}, error={}", imageId, e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("이미지 삭제 서버 오류: imageId={}", imageId, e);

            return ResponseEntity.internalServerError().body(Map.of(
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