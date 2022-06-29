package com.devit.devitcertificationservice.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class TokenDto {
    @ApiModelProperty(example = "accessToken")
    private final Object accessToken;
    @ApiModelProperty(example = "refreshToken")
    private final Object refreshToken;
}
