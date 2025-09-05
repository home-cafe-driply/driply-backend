package com.driply.backend.domains.product.controller;

import com.driply.backend.domains.product.dto.ProductResponseDTO;
import com.driply.backend.domains.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 공개 상품 API
 * 모든 사용자가 접근 가능한 상품 조회 기능
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * 상품 목록 조회 (페이징)
     * GET /api/products?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                    ? Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<ProductResponseDTO> products = productService.getAllProducts(pageable);

            log.info("상품 목록 조회: page={}, size={}, total={}", page, size, products.getTotalElements());

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            log.error("상품 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 카테고리별 상품 조회
     * GET /api/products/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                    ? Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<ProductResponseDTO> products = productService.getProductsByCategory(category, pageable);

            log.info("카테고리별 상품 조회: category={}, total={}", category, products.getTotalElements());

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            log.error("카테고리별 상품 조회 실패: category={}", category, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 상품 검색
     * GET /api/products/search?keyword=키워드
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                    ? Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<ProductResponseDTO> products = productService.searchProducts(keyword.trim(), pageable);

            log.info("상품 검색: keyword={}, total={}", keyword, products.getTotalElements());

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            log.error("상품 검색 실패: keyword={}", keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 상품 상세 조회
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            ProductResponseDTO product = productService.getProductById(id);

            log.info("상품 상세 조회: productId={}, name={}", id, product.getName());

            return ResponseEntity.ok(product);

        } catch (IllegalArgumentException e) {
            log.warn("상품 조회 실패 - 존재하지 않는 상품: productId={}", id);

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            log.error("상품 상세 조회 실패: productId={}", id, e);

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "서버 오류가 발생했습니다"
                    ));
        }
    }
}