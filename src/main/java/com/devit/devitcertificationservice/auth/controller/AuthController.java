package com.devit.devitcertificationservice.auth.controller;

import com.devit.devitcertificationservice.auth.dto.LoginDto;
import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.auth.service.AuthService;
import com.devit.devitcertificationservice.common.ResponseDetails;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "로그인", notes = "이메일, 비밀번호를 이용하여 로그인을 진행합니다.")
    public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginDto requestLoginDTO) {
        System.out.println("게이트웨이를 통해 들어왔습니다! :)");
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
    @ApiModelProperty(value = "토큰 갱신", notes = "refresh token 을 이용하여 새로운 access token을 발급받습니다.")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        ResponseDetails responseDetails = authService.refreshToken(request, response);
        return new ResponseEntity<>(responseDetails, HttpStatus.valueOf(responseDetails.getHttpStatus()));
    }
}
