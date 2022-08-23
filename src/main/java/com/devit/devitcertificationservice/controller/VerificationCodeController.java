package com.devit.devitcertificationservice.controller;

import com.devit.devitcertificationservice.aop.LoggingClientInfo;
import com.devit.devitcertificationservice.common.ResponseDetails;
import com.devit.devitcertificationservice.sevice.VerificationCodeMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.QueryParam;

@RestController
@RequiredArgsConstructor
@LoggingClientInfo
@Slf4j
public class VerificationCodeController {
    private final VerificationCodeMailService userService;

    //이메일 인증
    @GetMapping("/email")
    @ResponseBody
    public ResponseEntity<?> mailCheck(@QueryParam(value = "email") String email) {
        log.info("==이메일 인증 요청 시작==");
        log.info("이메일 인증 이메일 : {}", email);
        String code = userService.send(email);
        ResponseDetails responseDetails = ResponseDetails.success(code, "/api/auth/email");
        return new ResponseEntity<>(responseDetails, HttpStatus.OK);
    }
}
