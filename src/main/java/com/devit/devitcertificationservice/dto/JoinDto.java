package com.devit.devitcertificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class JoinDto {
    @Schema(description = "로그인 시 사용될 이메일", example = "dlekgp0423@naver.com")
    private final String email;
    @Schema(description = "로그인 시 사용될 패스워드", example = "1234")
    private final String password;
    @Schema(description = "사용자 닉네임", example = "이다혜")
    private final String nickName;
}
