package com.driply.backend.domains.member.seller.repository;

import com.driply.backend.domains.member.seller.entity.SellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<SellerEntity, Long> {

    Optional<SellerEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByBusinessNumber(String businessNumber);
}
