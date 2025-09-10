package com.driply.backend.domains.product.service;

import com.driply.backend.domains.product.dto.ProductImageDTO;
import com.driply.backend.domains.product.entity.ImageType;
import com.driply.backend.domains.product.entity.ProductEntity;
import com.driply.backend.domains.product.entity.ProductImageEntity;
import com.driply.backend.domains.product.repository.ProductImageRepository;
import com.driply.backend.domains.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageUploadService imageUploadService;

    /**
     * 상품 이미지 추가
     */
    @Transactional
    public ProductImageDTO addProductImage(Long sellerId, Long productId, MultipartFile file,
                                           ImageType imageType, boolean isThumbnail) {

        // 1. 권한 체크 (판매자가 자신의 상품인지 확인)
        ProductEntity product = productRepository.findProductByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없거나 권한이 없습니다"));

        // 2. 파일 업로드
        String folder = String.format("products/%d/%d", sellerId, productId);
        String imageUrl = imageUploadService.uploadImage(file, folder);

        // 3. 썸네일 설정 시 기존 썸네일 해제
        if (isThumbnail && imageType == ImageType.MAIN) {
            productImageRepository.unsetAllThumbnailsForProduct(productId);
        }

        // 4. 정렬 순서 계산
        int sortOrder = productImageRepository.findMaxSortOrderByProductIdAndType(productId, imageType) + 1;

        // 5. DB에 저장
        ProductImageEntity imageEntity = ProductImageEntity.builder()
                .productId(productId)
                .imageUrl(imageUrl)
                .altText(file.getOriginalFilename())
                .sortOrder(sortOrder)
                .isThumbnail(isThumbnail)
                .imageType(imageType)
                .build();

        ProductImageEntity savedImage = productImageRepository.save(imageEntity);

        log.info("상품 이미지 추가 완료: productId={}, imageId={}, type={}, thumbnail={}",
                productId, savedImage.getImageId(), imageType, isThumbnail);

        return convertToDTO(savedImage);
    }

    /**
     * 상품 이미지 삭제
     */
    @Transactional
    public void deleteProductImage(Long sellerId, Long imageId) {

        // 1. 이미지 조회 및 권한 체크
        ProductImageEntity image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다"));

        // 2. 상품 소유권 확인
        ProductEntity product = productRepository.findProductByIdAndSellerId(image.getProductId(), sellerId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없습니다"));

        // 3. 파일 삭제
        imageUploadService.deleteImage(image.getImageUrl());

        // 4. DB에서 논리적 삭제
        image.softDelete();
        productImageRepository.save(image);

        log.info("상품 이미지 삭제 완료: productId={}, imageId={}", image.getProductId(), imageId);
    }

    /**
     * Entity → DTO 변환
     */
    private ProductImageDTO convertToDTO(ProductImageEntity entity) {
        return ProductImageDTO.builder()
                .imageId(entity.getImageId())
                .imageUrl(entity.getImageUrl())
                .altText(entity.getAltText())
                .sortOrder(entity.getSortOrder())
                .isThumbnail(entity.getIsThumbnail())
                .imageType(entity.getImageType())
                .build();
    }
}