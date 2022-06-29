package com.devit.devitcertificationservice.user.dto;

import com.devit.devitcertificationservice.user.entity.Role;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class JoinDto {
    @ApiModelProperty(example = "회원 로그인 이메일")
    private final String email;
    @ApiModelProperty(example = "회원 로그인 비밀번호")
    private final String password;
    @ApiModelProperty(example = "회원 이름")
    private final String name;
    @ApiModelProperty(example = "회원 권한")
    private final String role;
}
