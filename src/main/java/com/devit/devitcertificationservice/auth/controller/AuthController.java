package com.devit.devitcertificationservice.auth.controller;

import com.devit.devitcertificationservice.auth.dto.LoginDto;
import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.auth.service.AuthService;
import com.devit.devitcertificationservice.common.ResponseDetails;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Api(tags = "Auth Controller")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginDto requestLoginDTO) {
        TokenDto token = authService.login(request, response, requestLoginDTO);
        ResponseDetails responseDetails;
        if (token == null) {
            responseDetails = ResponseDetails.loginFail("로그인 실패", "/api/auth/login");
            return new ResponseEntity<>(responseDetails, HttpStatus.UNAUTHORIZED);
        }
        responseDetails = ResponseDetails.success(token, "/api/auth/login");
        return new ResponseEntity<>(responseDetails, HttpStatus.OK);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        ResponseDetails responseDetails = authService.refreshToken(request, response);
        return new ResponseEntity<>(responseDetails, HttpStatus.valueOf(responseDetails.getHttpStatus()));
    }
}
