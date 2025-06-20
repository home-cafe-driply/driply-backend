package com.driply.backend.domains.member.customer.repository;

import com.driply.backend.domains.member.customer.entity.CustomerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerRepositoryBasicTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("이메일로 고객을 조회할 수 있다")
    void findByEmail_Success() {
        // given
        CustomerEntity customer = CustomerEntity.builder()
                .email("test@gmail.com")
                .password("password")
                .name("Tester")
                .nickname("testNickname")
                .phone("010-1234-5678")
                .build();

        customerRepository.save(customer);

        // when
        Optional<CustomerEntity> result = customerRepository.findByEmail("test@gmail.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.get().getPassword()).isEqualTo("password");
        assertThat(result.get().getName()).isEqualTo("Tester");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회")
    void findByEmail_NotFound() {
        // when
        Optional<CustomerEntity> result = customerRepository.findByEmail("notexist@example.com");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일 중복 체크 - 존재하는 경우")
    void existsByEmail_True() {
        // given
        CustomerEntity customer = CustomerEntity.builder()
                .email("exist@example.com")
                .password("password123")
                .name("기존사용자")
                .nickname("existnick")
                .build();

        customerRepository.save(customer);

        // when
        boolean exists = customerRepository.existsByEmail("exist@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 체크 - 존재하지 않는 경우")
    void existsByEmail_False() {
        // when
        boolean exists = customerRepository.existsByEmail("notexist@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 존재하는 경우")
    void existsByNickname_True() {
        // given
        CustomerEntity customer = CustomerEntity.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트사용자")
                .nickname("existnick")
                .build();

        customerRepository.save(customer);

        // when
        boolean exists = customerRepository.existsByNickname("existnick");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 존재하지 않는 경우")
    void existsByNickname_False() {
        // when
        boolean exists = customerRepository.existsByNickname("newnick");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("고객 저장 후 기본값들이 올바르게 설정된다")
    void save_Customer_DefaultValues() {
        // given
        CustomerEntity customer = CustomerEntity.builder()
                .email("save@example.com")
                .password("password123")
                .name("저장테스트")
                .nickname("savenick")
                .build();

        // when
        CustomerEntity saved = customerRepository.save(customer);

        // then
        assertThat(saved.getCustomerId()).isNotNull();
        assertThat(saved.isPassed()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("논리적 삭제 기능이 정상 동작한다")
    void softDelete_Works() {
        // given
        CustomerEntity customer = CustomerEntity.builder()
                .email("delete@example.com")
                .password("password123")
                .name("삭제테스트")
                .nickname("deletenick")
                .build();

        CustomerEntity saved = customerRepository.save(customer);

        // when
        saved.softDelete();
        customerRepository.save(saved);

        // then
        Optional<CustomerEntity> found = customerRepository.findById(saved.getCustomerId());
        assertThat(found).isPresent();
        assertThat(found.get().isActive()).isFalse();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }
}