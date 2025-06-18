package com.driply.backend.domains.member.customer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class JoinDTO {

    private String email;
    private String userId;
    private String pw;
    private String pwConfirm;
    private String name;
    private LocalDate birth;
    private String phone;
}
