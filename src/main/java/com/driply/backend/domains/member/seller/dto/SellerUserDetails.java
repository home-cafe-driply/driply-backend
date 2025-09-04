package com.driply.backend.domains.member.seller.dto;

import com.driply.backend.domains.member.seller.entity.SellerEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class SellerUserDetails implements UserDetails {

    private final SellerEntity sellerEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        return collection;
    }

    @Override
    public String getPassword() {
        return sellerEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return sellerEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return sellerEntity.isActive(); // 활성 상태면 로그인 허용 (인증 여부 무관)
    }

    /**
     * 판매자 ID 반환
     */
    public Long getSellerId() {
        return sellerEntity.getSellerId();
    }

    /**
     * 회사명 반환
     */
    public String getCompanyName() {
        return sellerEntity.getCompanyName();
    }

    /**
     * 사업자등록번호 반환
     */
    public String getBusinessNumber() {
        return sellerEntity.getBusinessNumber();
    }

    /**
     * 인증 여부 확인
     */
    public boolean isVerified() {
        return sellerEntity.isVerified();
    }
}