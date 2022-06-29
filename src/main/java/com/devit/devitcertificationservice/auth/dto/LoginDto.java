package com.devit.devitcertificationservice.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class LoginDto {
    @ApiModelProperty(example = "회원 로그인 이메일")
    private final String email;
    @ApiModelProperty(example = "회원 로그인 비밀번호")
    private final String password;
}
