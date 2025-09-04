package com.driply.backend.domains.member.customer.controller;

import com.driply.backend.domains.member.customer.dto.JoinDTO;
import com.driply.backend.domains.member.customer.service.JoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class JoinController {

    private final JoinService joinService;

    /**
     * 고객 회원가입
     * @param joinDTO 회원가입 정보 (이메일, 비밀번호, 이름, 닉네임, 전화번호)
     * @return 회원가입 결과
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinProcess(@RequestBody JoinDTO joinDTO) {
        try {
            log.info("고객 회원가입 요청: email={}, nickname={}", joinDTO.getEmail(), joinDTO.getNickname());

            joinService.joinProcess(joinDTO);

            log.info("고객 회원가입 성공: email={}", joinDTO.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "회원가입이 완료되었습니다.",
                            "email", joinDTO.getEmail()
                    ));

        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패 - 입력값 오류: email={}, error={}", joinDTO.getEmail(), e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {
            log.error("회원가입 서버 오류: email={}", joinDTO.getEmail(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    ));
        }
    }
}

