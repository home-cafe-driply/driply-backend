package com.driply.backend.domains.product.repository;

import com.driply.backend.domains.product.entity.ProductEntity;
import com.driply.backend.domains.product.entity.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository 기본 테스트")
class ProductRepositoryBasicTest {

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity activeProduct1;
    private ProductEntity activeProduct2;
    private ProductEntity inactiveProduct;
    private ProductEntity deletedProduct;

    @BeforeEach
    void setUp() {
        // 활성 상품 1
        activeProduct1 = ProductEntity.builder()
                .sellerId(1L)
                .name("테스트 상품 1")
                .description("테스트 상품 1 설명")
                .basePrice(new BigDecimal("10000"))
                .stockQuantity(100)
                .category("전자제품")
                .status(ProductStatus.ACTIVE)
                .build();

        // 활성 상품 2 (다른 카테고리)
        activeProduct2 = ProductEntity.builder()
                .sellerId(2L)
                .name("아이폰 케이스")
                .description("고급 아이폰 케이스입니다")
                .basePrice(new BigDecimal("25000"))
                .stockQuantity(50)
                .category("액세서리")
                .status(ProductStatus.ACTIVE)
                .build();

        // 비활성 상품
        inactiveProduct = ProductEntity.builder()
                .sellerId(1L)
                .name("비활성 상품")
                .description("판매중지된 상품")
                .basePrice(new BigDecimal("15000"))
                .stockQuantity(0)
                .category("전자제품")
                .status(ProductStatus.INACTIVE)
                .build();

        // 논리 삭제된 상품
        deletedProduct = ProductEntity.builder()
                .sellerId(1L)
                .name("삭제된 상품")
                .description("삭제된 상품")
                .basePrice(new BigDecimal("20000"))
                .stockQuantity(10)
                .category("전자제품")
                .status(ProductStatus.ACTIVE)
                .build();

        // DB에 저장
        productRepository.save(activeProduct1);
        productRepository.save(activeProduct2);
        productRepository.save(inactiveProduct);

        deletedProduct = productRepository.save(deletedProduct);
        deletedProduct.softDelete(); // 논리 삭제
        productRepository.save(deletedProduct);
    }

    @Test
    @DisplayName("활성 상품만 조회 - 성공")
    void findActiveProducts_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ProductEntity> result = productRepository.findActiveProducts(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("name")
                .containsExactlyInAnyOrder("테스트 상품 1", "아이폰 케이스");

        // 모든 상품이 ACTIVE 상태인지 확인
        assertThat(result.getContent()).allMatch(product ->
                product.getStatus() == ProductStatus.ACTIVE && product.isActive());
    }

    @Test
    @DisplayName("카테고리별 활성 상품 조회 - 성공")
    void findActiveProductsByCategory_Success() {
        // given
        String category = "전자제품";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ProductEntity> result = productRepository.findActiveProductsByCategory(category, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 상품 1");
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("전자제품");
    }

    @Test
    @DisplayName("상품명으로 검색 - 성공")
    void searchActiveProducts_ByName_Success() {
        // given
        String keyword = "아이폰";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ProductEntity> result = productRepository.searchActiveProducts(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).contains("아이폰");
    }

    @Test
    @DisplayName("상품 설명으로 검색 - 성공")
    void searchActiveProducts_ByDescription_Success() {
        // given
        String keyword = "고급";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ProductEntity> result = productRepository.searchActiveProducts(keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).contains("고급");
    }

    @Test
    @DisplayName("활성 상품 ID로 조회 - 성공")
    void findActiveProductById_Success() {
        // given
        Long productId = activeProduct1.getProductId();

        // when
        Optional<ProductEntity> result = productRepository.findActiveProductById(productId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("테스트 상품 1");
        assertThat(result.get().getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("비활성 상품 ID로 조회 - 실패")
    void findActiveProductById_InactiveProduct_NotFound() {
        // given
        Long productId = inactiveProduct.getProductId();

        // when
        Optional<ProductEntity> result = productRepository.findActiveProductById(productId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("삭제된 상품 ID로 조회 - 실패")
    void findActiveProductById_DeletedProduct_NotFound() {
        // given
        Long productId = deletedProduct.getProductId();

        // when
        Optional<ProductEntity> result = productRepository.findActiveProductById(productId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("판매자별 상품 조회 - 성공")
    void findProductsBySellerId_Success() {
        // given
        Long sellerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ProductEntity> result = productRepository.findProductsBySellerId(sellerId, pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // activeProduct1 + inactiveProduct (deletedProduct는 제외)
        assertThat(result.getContent()).extracting("sellerId")
                .containsOnly(1L);
    }

    @Test
    @DisplayName("판매자 ID와 상품 ID로 조회 - 성공")
    void findProductByIdAndSellerId_Success() {
        // given
        Long productId = activeProduct1.getProductId();
        Long sellerId = 1L;

        // when
        Optional<ProductEntity> result = productRepository.findProductByIdAndSellerId(productId, sellerId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("테스트 상품 1");
        assertThat(result.get().getSellerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("다른 판매자 상품 조회 - 실패")
    void findProductByIdAndSellerId_WrongSeller_NotFound() {
        // given
        Long productId = activeProduct1.getProductId();
        Long wrongSellerId = 999L;

        // when
        Optional<ProductEntity> result = productRepository.findProductByIdAndSellerId(productId, wrongSellerId);

        // then
        assertThat(result).isEmpty();
    }
}