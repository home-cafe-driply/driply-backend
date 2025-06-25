package com.driply.backend.domains.member.customer.dto;

import com.driply.backend.domains.member.customer.entity.CustomerEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final CustomerEntity customerEntity;

    public CustomUserDetails(CustomerEntity customerEntity) {
        this.customerEntity = customerEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_USER"; // Customer는 모두 ROLE_USER (JWT로 Customer/Seller 구분)
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        return customerEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return customerEntity.getEmail(); // username 대신 email 사용
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
        return customerEntity.isActive(); // CustomerEntity의 isActive() 메서드 활용
    }
}