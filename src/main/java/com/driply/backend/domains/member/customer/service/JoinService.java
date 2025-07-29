package com.driply.backend.domains.member.customer.service;

import com.driply.backend.domains.member.customer.dto.JoinDTO;
import com.driply.backend.domains.member.customer.entity.CustomerEntity;
import com.driply.backend.domains.member.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JoinService {

    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(JoinDTO joinDTO) {
        String email = joinDTO.getEmail();
        String nickname = joinDTO.getNickname();
        String password = joinDTO.getPassword();

        // 이메일 중복 체크
        Boolean isEmailExist = customerRepository.existsByEmail(email);
        if (isEmailExist) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        // 닉네임 중복 체크
        Boolean isNicknameExist = customerRepository.existsByNickname(nickname);
        if (isNicknameExist) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        // CustomerEntity 생성 및 저장
        CustomerEntity customer = CustomerEntity.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .name(joinDTO.getName())
                .nickname(nickname)
                .phone(joinDTO.getPhone())
                .build();

        customerRepository.save(customer);
    }
}
