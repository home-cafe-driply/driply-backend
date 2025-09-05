package com.driply.backend.domains.product.service;

import com.driply.backend.domains.product.dto.ProductImageDTO;
import com.driply.backend.domains.product.dto.ProductRequestDTO;
import com.driply.backend.domains.product.dto.ProductResponseDTO;
import com.driply.backend.domains.product.entity.ProductEntity;
import com.driply.backend.domains.product.entity.ProductImageEntity;
import com.driply.backend.domains.product.entity.ProductStatus;
import com.driply.backend.domains.product.repository.ProductImageRepository;
import com.driply.backend.domains.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 상품 목록 조회 (공개 API)
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        Page<ProductEntity> products = productRepository.findActiveProducts(pageable);
        return products.map(this::convertToResponseDTO);
    }

    /**
     * 카테고리별 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByCategory(String category, Pageable pageable) {
        Page<ProductEntity> products = productRepository.findActiveProductsByCategory(category, pageable);
        return products.map(this::convertToResponseDTO);
    }

    /**
     * 상품 검색
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(String keyword, Pageable pageable) {
        Page<ProductEntity> products = productRepository.searchActiveProducts(keyword, pageable);
        return products.map(this::convertToResponseDTO);
    }

    /**
     * 상품 상세 조회 (공개 API)
     */
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long productId) {
        ProductEntity product = productRepository.findActiveProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        return convertToResponseDTOWithImages(product);
    }

    /**
     * 판매자의 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsBySeller(Long sellerId, Pageable pageable) {
        Page<ProductEntity> products = productRepository.findProductsBySellerId(sellerId, pageable);
        return products.map(this::convertToResponseDTO);
    }

    /**
     * 상품 등록 (판매자용)
     */
    @Transactional
    public ProductResponseDTO createProduct(Long sellerId, ProductRequestDTO dto) {
        // 유효성 검증
        validateProductRequest(dto);

        ProductEntity product = ProductEntity.builder()
                .sellerId(sellerId)
                .name(dto.getName())
                .description(dto.getDescription())
                .basePrice(dto.getBasePrice())
                .stockQuantity(dto.getStockQuantity())
                .category(dto.getCategory())
                .metadata(dto.getMetadata())
                .status(ProductStatus.ACTIVE)
                .build();

        ProductEntity savedProduct = productRepository.save(product);

        log.info("상품 등록 완료: productId={}, sellerId={}, name={}",
                savedProduct.getProductId(), sellerId, savedProduct.getName());

        return convertToResponseDTO(savedProduct);
    }

    /**
     * 상품 수정 (판매자용)
     */
    @Transactional
    public ProductResponseDTO updateProduct(Long sellerId, Long productId, ProductRequestDTO dto) {
        // 권한 체크 + 상품 조회
        ProductEntity product = productRepository.findProductByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 권한이 없거나 상품을 찾을 수 없습니다"));

        // 부분 업데이트
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            product = product.toBuilder().name(dto.getName()).build();
        }
        if (dto.getDescription() != null) {
            product = product.toBuilder().description(dto.getDescription()).build();
        }
        if (dto.getBasePrice() != null) {
            product = product.toBuilder().basePrice(dto.getBasePrice()).build();
        }
        if (dto.getStockQuantity() != null) {
            product = product.toBuilder().stockQuantity(dto.getStockQuantity()).build();
        }
        if (dto.getCategory() != null) {
            product = product.toBuilder().category(dto.getCategory()).build();
        }
        if (dto.getMetadata() != null) {
            product = product.toBuilder().metadata(dto.getMetadata()).build();
        }

        ProductEntity updatedProduct = productRepository.save(product);

        log.info("상품 수정 완료: productId={}, sellerId={}", productId, sellerId);

        return convertToResponseDTO(updatedProduct);
    }

    /**
     * 상품 삭제 (판매자용)
     */
    @Transactional
    public void deleteProduct(Long sellerId, Long productId) {
        ProductEntity product = productRepository.findProductByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 권한이 없거나 상품을 찾을 수 없습니다"));

        // 논리적 삭제 (BaseEntity의 softDelete 사용)
        product.softDelete();
        productRepository.save(product);

        log.info("상품 삭제 완료: productId={}, sellerId={}", productId, sellerId);
    }

    /**
     * Entity -> ResponseDTO 변환 (이미지 포함)
     */
    private ProductResponseDTO convertToResponseDTOWithImages(ProductEntity product) {
        List<ProductImageEntity> images = productImageRepository.findActiveImagesByProductId(product.getProductId());
        List<ProductImageDTO> imageDTOs = images.stream()
                .map(this::convertToImageDTO)
                .collect(Collectors.toList());

        String thumbnailUrl = images.stream()
                .filter(ProductImageEntity::isThumbnailImage)
                .findFirst()
                .map(ProductImageEntity::getImageUrl)
                .orElse(images.isEmpty() ? null : images.getFirst().getImageUrl());

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .status(product.getStatus())
                .metadata(product.getMetadata())
                .sellerId(product.getSellerId())
                .images(imageDTOs)
                .thumbnailUrl(thumbnailUrl)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Entity -> ResponseDTO 변환 (이미지 제외, 목록용)
     */
    private ProductResponseDTO convertToResponseDTO(ProductEntity product) {
        // 썸네일만 조회
        String thumbnailUrl = productImageRepository.findThumbnailByProductId(product.getProductId())
                .map(ProductImageEntity::getImageUrl)
                .orElse(null);

        return ProductResponseDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .status(product.getStatus())
                .metadata(product.getMetadata())
                .sellerId(product.getSellerId())
                .thumbnailUrl(thumbnailUrl)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * ImageEntity -> ImageDTO 변환
     */
    private ProductImageDTO convertToImageDTO(ProductImageEntity image) {
        return ProductImageDTO.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .isThumbnail(image.getIsThumbnail())
                .imageType(image.getImageType())
                .build();
    }

    /**
     * 상품 등록 유효성 검증
     */
    private void validateProductRequest(ProductRequestDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (dto.getBasePrice() == null || dto.getBasePrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }
        if (dto.getStockQuantity() == null || dto.getStockQuantity() < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다");
        }
    }
}