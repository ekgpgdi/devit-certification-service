package com.devit.devitcertificationservice.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Role {
    GENERAL("GENERAL", "일반 회원 권한"),
    ADMIN("ADMIN", "관리자 권한");

    @Schema(example = "회원 권한")
    private final String code;
    @Schema(example = "회원 권한 설명")
    private final String displayName;

    public static Role of(String code) {
        return Arrays.stream(Role.values())
                .filter(r -> r.getCode().equals(code))
                .findAny()
                .orElse(GENERAL);
    }
}
