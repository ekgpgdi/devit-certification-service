package com.devit.devitcertificationservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Type {
    GENERAL("GENERAL", "일반 가입 회원"),
    SOCIAL("SOCIAL", "소셜 가입 회원");

    @Schema(example = "회원가입 종류 -> 일반/소셜")
    private final String code;
    @Schema(example = "회원가입 종류 설명")
    private final String displayName;

    public static Type of(String code) {
        return Arrays.stream(Type.values())
                .filter(r -> r.getCode().equals(code))
                .findAny()
                .orElse(GENERAL);
    }
}
