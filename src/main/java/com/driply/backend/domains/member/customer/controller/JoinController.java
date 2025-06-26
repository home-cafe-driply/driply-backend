package com.driply.backend.domains.member.customer.controller;

import com.driply.backend.domains.member.customer.dto.JoinDTO;
import com.driply.backend.domains.member.customer.service.JoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/join")
    public String joinProcess(JoinDTO joinDTO) {
        System.out.println(joinDTO.getEmail());
        joinService.joinProcess(joinDTO);
        return "ok";
    }
}

