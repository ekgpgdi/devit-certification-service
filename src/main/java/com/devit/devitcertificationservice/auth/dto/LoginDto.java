package com.devit.devitcertificationservice.auth.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Schema(description = "로그인 DTO")
public class LoginDto {
    @Schema(description = "로그인 시 사용될 이메일", example = "dlekgp0423@naver.com")
    private final String email;
    @Schema(description = "로그인 시 사용될  비밀번호", example = "1234")
    private final String password;
}
