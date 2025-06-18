//package com.driply.backend.domains.member.customer.repository;
//
//import com.driply.backend.domains.member.customer.entity.CustomerEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
//
//    /**
//     * 사용자 ID로 고객 정보를 조회합니다.
//     * 로그인 시 사용자 ID 확인에 활용됩니다.
//     *
//     * @param userId 조회할 사용자 ID
//     * @return 일치하는 고객 정보 (Optional)
//     */
//    Optional<CustomerEntity> findByUserId(String userId);
//
//    /**
//     * 이메일 주소로 고객 정보를 조회합니다.
//     * 비밀번호 재설정, 이메일 중복 체크 등에 활용됩니다.
//     *
//     * @param email 조회할 이메일 주소
//     * @return 일치하는 고객 정보 (Optional)
//     */
//    Optional<CustomerEntity> findByEmail(String email);
//
//    /**
//     * 닉네임으로 고객 정보를 조회합니다.
//     * 사용자 프로필 조회, 닉네임 중복 체크 등에 활용됩니다.
//     *
//     * @param nickname 조회할 닉네임
//     * @return 일치하는 고객 정보 (Optional)
//     */
//    Optional<CustomerEntity> findByNickname(String nickname);
//
//    /**
//     * 전화번호로 고객 정보를 조회합니다.
//     * 본인 인증, 전화번호 중복 체크 등에 활용됩니다.
//     *
//     * @param phone 조회할 전화번호
//     * @return 일치하는 고객 정보 (Optional)
//     */
//    Optional<CustomerEntity> findByPhone(String phone);
//
//    /**
//     * 사용자 ID 존재 여부를 확인합니다.
//     * 회원가입 시 ID 중복 체크 등에 활용됩니다.
//     *
//     * @param userId 확인할 사용자 ID
//     * @return 존재 여부 (true/false)
//     */
//    boolean existsByUserId(String userId);
//
//    /**
//     * 이메일 주소 존재 여부를 확인합니다.
//     * 회원가입 시 이메일 중복 체크 등에 활용됩니다.
//     *
//     * @param email 확인할 이메일 주소
//     * @return 존재 여부 (true/false)
//     */
//    boolean existsByEmail(String email);
//
//    /**
//     * 닉네임 존재 여부를 확인합니다.
//     * 회원가입이나 프로필 수정 시 닉네임 중복 체크에 활용됩니다.
//     *
//     * @param nickname 확인할 닉네임
//     * @return 존재 여부 (true/false)
//     */
//    boolean existsByNickname(String nickname);
//
//    /**
//     * 전화번호 존재 여부를 확인합니다.
//     * 본인 인증 시 전화번호 중복 체크 등에 활용됩니다.
//     *
//     * @param phone 확인할 전화번호
//     * @return 존재 여부 (true/false)
//     */
//    boolean existsByPhone(String phone);
//
//    /**
//     * 인증 상태에 따라 고객 목록을 조회합니다.
//     * 관리자 기능에서 인증된/인증되지 않은 사용자 목록 확인 등에 활용됩니다.
//     *
//     * @param isPassed 인증 상태 (true: 인증됨, false: 인증되지 않음)
//     * @return 해당 인증 상태의 고객 목록
//     */
//    List<CustomerEntity> findByIsPassed(Boolean isPassed);
//
//    /**
//     * 이름과 전화번호로 고객 정보를 조회합니다.
//     * ID 찾기, 비밀번호 재설정 등 본인 확인 과정에 활용됩니다.
//     *
//     * @param name  확인할 이름
//     * @param phone 확인할 전화번호
//     * @return 일치하는 고객 정보 (Optional)
//     */
//    Optional<CustomerEntity> findByNameAndPhone(String name, String phone);
//
//    /**
//     * 이름과 이메일로 고객 정보를 조회합니다.
//     * ID 찾기, 비밀번호 재설정 등 본인 확인 과정에 활용됩니다.
//     *
//     * @param name  확인할 이름
//     * @param email 확인할 이메일
//     * @return 일치하는 고객 정보 (Optional)
//     */
//    Optional<CustomerEntity> findByNameAndEmail(String name, String email);
//
//}
