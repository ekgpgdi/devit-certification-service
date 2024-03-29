package com.devit.devitcertificationservice.controller;

import com.devit.devitcertificationservice.aop.LoggingClientInfo;
import com.devit.devitcertificationservice.common.ResponseDetails;
import com.devit.devitcertificationservice.dto.JoinDto;
import com.devit.devitcertificationservice.dto.LoginDto;
import com.devit.devitcertificationservice.dto.TokenDto;
import com.devit.devitcertificationservice.entity.Type;
import com.devit.devitcertificationservice.sevice.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@LoggingClientInfo
@Slf4j
public class AuthController {
    private final AuthService authService;

    @GetMapping("/duplicate-check")
    @Operation(summary = "이메일 중복 체크", description = "이메일이 중복되는 경우 true 를 반환합니다.", responses = {
            @ApiResponse(responseCode = "200", description = "이메일 중복 체크 성공", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "이메일 중복 체크 성공 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-06-28T03:26:30.984+00:00\",\n" +
                                    "    \"data\": false,\n" +
                                    "    \"httpStatus\": 200,\n" +
                                    "    \"path\": \"/api/auth/duplicate-check\"\n" +
                                    "}"
                    )))
    }
    )
    public ResponseEntity<?> duplicateCheck(HttpServletRequest request, HttpServletResponse response, @RequestParam String email) {
        log.info("==이메일 중복 체크 요청 시작==");
        Boolean duplicate = authService.emailDuplicate(email);
        ResponseDetails responseDetails = ResponseDetails.success(duplicate, "/api/auth/duplicate-check");
        return new ResponseEntity<>(responseDetails, HttpStatus.OK);
    }

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
        log.info("== email : {} 로그인 요청 시작 ==", requestLoginDTO.getEmail());
        TokenDto token = authService.login(request, response, requestLoginDTO);
        ResponseDetails responseDetails;
        if (token == null) {
            log.info("email : {} 로그인 실패", requestLoginDTO.getEmail());
            responseDetails = ResponseDetails.loginFail("로그인 실패", "/api/auth/login");
            return new ResponseEntity<>(responseDetails, HttpStatus.UNAUTHORIZED);
        }
        log.info("email : {} 로그인 성공, accessToken : {}", requestLoginDTO.getEmail(), token.getAccessToken());
        responseDetails = ResponseDetails.success(token, "/api/auth/login");
        return new ResponseEntity<>(responseDetails, HttpStatus.OK);
    }

    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "이 API로 회원가입을 하는 경우 Role은 GENERAL이 됩니다.", responses = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "회원가입 성공 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-07-07T03:25:01.442+00:00\",\n" +
                                    "    \"data\": {\n" +
                                    "        \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjU3MTY0MzAxLCJyb2xlIjoiR0VORVJBTCIsInVpZCI6ImRmYjJjYzE2LWQ5ZGQtNDk5NS1hMjU3LTcxMTNkOWViMGY4ZSIsImVtYWlsIjoiZGxla2dwMDQyM0BuYXZlci5jb20iLCJuaWNrTmFtZSI6IuydtOuLpO2YnCIsImV4cCI6MTY1NzE3NTEwMX0.rFv_nMkEDCLbHh7sqlP-ZbQPRz-a3brrzS2wOEJIWwY\"\n" +
                                    "    },\n" +
                                    "    \"httpStatus\": 201,\n" +
                                    "    \"path\": \"/api/auth/join\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "400", description = "이메일 중복으로 인한 회원가입 실패", content = @Content(
                    schema = @Schema(requiredProperties = {"2022-07-07T05:57:33.095+00:00", "회원가입 실패(이메일 중복)", ""}),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "회원가입 실패 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-07-07T05:57:33.095+00:00\",\n" +
                                    "    \"data\": \"회원가입 실패(이메일 중복)\",\n" +
                                    "    \"httpStatus\": 400,\n" +
                                    "    \"path\": \"/api/auth/join\"\n" +
                                    "}")))
    })
    public ResponseEntity<?> join(HttpServletRequest request, HttpServletResponse response, @RequestBody JoinDto requestJoinDTO) {
        log.info("==회원가입 요청 시작==");
        TokenDto token = authService.join(request, response, requestJoinDTO, Type.GENERAL);
        ResponseDetails responseDetails;
        if (token == null) {
            log.info("이메일 중복으로 회원가입을 실패하였습니다. [중복된 email:{}]", requestJoinDTO.getEmail());
            responseDetails = ResponseDetails.badRequest("회원가입 실패(이메일 중복)", "/api/auth/join");
            return new ResponseEntity<>(responseDetails, HttpStatus.BAD_REQUEST);
        }
        responseDetails = ResponseDetails.created(token, "/api/auth/join");
        return new ResponseEntity<>(responseDetails, HttpStatus.CREATED);
    }

    @GetMapping("/kakao")
    @Operation(summary = "카카오 소셜 로그인/회원가입", description = "카카오에서 받은 인가 코드로 로그인/회원가입을 진행합니다.", responses = {
            @ApiResponse(responseCode = "200", description = "카카오 소셜 로그인/회원가입 성공", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "카카오 소셜 로그인 성공 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-07-07T03:25:01.442+00:00\",\n" +
                                    "    \"data\": {\n" +
                                    "        \"accessToken\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjU3MTY0MzAxLCJyb2xlIjoiR0VORVJBTCIsInVpZCI6ImRmYjJjYzE2LWQ5ZGQtNDk5NS1hMjU3LTcxMTNkOWViMGY4ZSIsImVtYWlsIjoiZGxla2dwMDQyM0BuYXZlci5jb20iLCJuaWNrTmFtZSI6IuydtOuLpO2YnCIsImV4cCI6MTY1NzE3NTEwMX0.rFv_nMkEDCLbHh7sqlP-ZbQPRz-a3brrzS2wOEJIWwY\"\n" +
                                    "    },\n" +
                                    "    \"httpStatus\": 200,\n" +
                                    "    \"path\": \"/api/auth/kakao\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "401", description = "로그인 시 비밀번호가 맞지 않음", content = @Content(
                    schema = @Schema(implementation = ResponseEntity.class),
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "카카오 소셜 로그인 실패 응답 샘플",
                            value = "{\n" +
                                    "    \"timestamp\": \"2022-07-07T05:57:33.095+00:00\",\n" +
                                    "    \"data\": \"카카오 소셜 로그인 실패\",\n" +
                                    "    \"httpStatus\": 401,\n" +
                                    "    \"path\": \"/api/auth/kakao\"\n" +
                                    "}")))
    })
    public ResponseEntity<?> kakaoLogin(@RequestParam String code, HttpServletResponse response, HttpServletRequest request) {
        log.info("==카카오 로그인/회원가입 요청 시작==");
        // authorizedCode: 카카오 서버로부터 받은 인가 코드
        TokenDto token = authService.kakao(code, response, request);
        ResponseDetails responseDetails;
        if (token == null) {
            responseDetails = ResponseDetails.loginFail("카카오 소셜 로그인 실패", "/api/auth/kakao");
            return new ResponseEntity<>(responseDetails, HttpStatus.UNAUTHORIZED);
        }
        responseDetails = ResponseDetails.success(token, "/api/auth/kakao");
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
        log.info("== 토큰 갱신 요청 시작 == ");
        ResponseDetails responseDetails = authService.refreshToken(request, response);
        return new ResponseEntity<>(responseDetails, HttpStatus.valueOf(responseDetails.getHttpStatus()));
    }
}
