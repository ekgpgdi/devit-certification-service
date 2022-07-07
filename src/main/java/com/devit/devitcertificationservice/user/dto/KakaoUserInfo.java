package com.devit.devitcertificationservice.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KakaoUserInfo {
    @Schema(description = "카카오에서 주는 ID", example = "1234567890")
    Long id;
    @Schema(description = "카카오 email", example = "dlekgp0423@naver.com")
    String email;
    @Schema(description = "카카오 이름", example = "다혜")
    String nickname;
}
