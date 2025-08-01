package com.driply.backend.domains.member.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerJoinDTO {
    private String email;
    private String password;
    private String companyName;
    private String businessNumber;
}
