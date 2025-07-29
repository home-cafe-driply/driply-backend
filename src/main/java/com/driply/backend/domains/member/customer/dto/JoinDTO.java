package com.driply.backend.domains.member.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinDTO {

    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
}
