package com.devit.devitcertificationservice.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Type {
    GENERAL("GENERAL", "일반 가입 회원"),
    SOCIAL("ADMIN", "소셜 가입 회원");

    private final String code;
    private final String displayName;

    public static Type of(String code) {
        return Arrays.stream(Type.values())
                .filter(r -> r.getCode().equals(code))
                .findAny()
                .orElse(GENERAL);
    }
}
