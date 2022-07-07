package com.devit.devitcertificationservice.auth.controller;

import com.devit.devitcertificationservice.auth.dto.LoginDto;
import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.auth.service.AuthService;
import com.devit.devitcertificationservice.common.ResponseDetails;
import com.devit.devitcertificationservice.user.entity.UserCertification;
import com.devit.devitcertificationservice.user.repository.UserCertificationRepository;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserCertificationRepository userCertificationRepository;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일, 비밀번호를 이용하여 로그인을 진행합니다.", responses = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "로그인 성공 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-07-07T03:25:22.476+00:00\",\n" +
                                    "    \"data\": {\n" +
                                    "        \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjU3MTY0MzIyLCJyb2xlIjoiR0VORVJBTCIsInVpZCI6ImRmYjJjYzE2LWQ5ZGQtNDk5NS1hMjU3LTcxMTNkOWViMGY4ZSIsImVtYWlsIjoiZGxla2dwMDQyM0BuYXZlci5jb20iLCJuaWNrTmFtZSI6IuydtOuLpO2YnCIsImV4cCI6MTY1NzE3NTEyMn0.9Bop-kamqep33TYp4nxjdFvEWDZjGw-dLEK-ok22w40\"\n" +
                                    "    },\n" +
                                    "    \"httpStatus\": 200,\n" +
                                    "    \"path\": \"/api/auth/login\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "401", description = "로그인 실패", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "로그인 실패 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-06-28T03:05:26.747+00:00\",\n" +
                                    "    \"data\": \"로그인 실패\",\n" +
                                    "    \"httpStatus\": 401,\n" +
                                    "    \"path\": \"/api/auth/login\"\n" +
                                    "}"
                    ))),
    })
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
    @Operation(summary = "토큰 갱신", description = "refresh token 을 이용하여 새로운 access token 을 발급받습니다.", responses = {
            @ApiResponse(responseCode = "200", description = "새로운 Access token 발급 및 Refresh token 갱신 성공", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "토큰 갱신 성공 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-06-28T12:54:24.883+00:00\",\n" +
                                    "    \"data\": {\n" +
                                    "        \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjU3MTU4NzIzLCJyb2xlIjoiR0VORVJBTCIsInVpZCI6IjlmMjdiNzQ5LWJkOTktNDBmZC05MzgzLTY4ZmI4YTllODk2NiIsImVtYWlsIjoiZGxla2dwMDQyM0BuYXZlci5jb20iLCJuaWNrTmFtZSI6IuydtOuLpO2YnCIsImV4cCI6MTY1NzE2OTUyM30.HM1yIcTvHMT7lTypikK5pttLhGYa6rqunvaRhxrFGLM\",\n" +
                                    "    },\n" +
                                    "    \"httpStatus\": 200,\n" +
                                    "    \"path\": \"/api/auth/refresh\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "202", description = "Refresh token 이 유효하지 않음", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Refresh token 이 유효하지 않은 경우 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-06-28T11:07:17.784+00:00\",\n" +
                                    "    \"data\": \"Invalid refresh token.\",\n" +
                                    "    \"httpStatus\": 202,\n" +
                                    "    \"path\": \"/api/auth/refresh\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "203", description = "Access token 이 만료되지 않음", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Access token 이 만료되지 않은 경우 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-06-28T08:58:17.106+00:00\",\n" +
                                    "    \"data\": \"Invalid access token.\",\n" +
                                    "    \"httpStatus\": 500,\n" +
                                    "    \"path\": \"/api/auth/refresh\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Access token 이 유효하지 않음", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Access token 유효하지 않은 경우 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-06-28T08:58:17.106+00:00\",\n" +
                                    "    \"data\": \"Invalid access token.\",\n" +
                                    "    \"httpStatus\": 500,\n" +
                                    "    \"path\": \"/api/auth/refresh\"\n" +
                                    "}"
                    ))),
    })
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        ResponseDetails responseDetails = authService.refreshToken(request, response);
        return new ResponseEntity<>(responseDetails, HttpStatus.valueOf(responseDetails.getHttpStatus()));
    }
}
