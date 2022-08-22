package com.devit.devitcertificationservice.user.entity;

import com.devit.devitcertificationservice.auth.util.Timestamped;
import com.devit.devitcertificationservice.user.dto.JoinDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserCertification extends Timestamped {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "자동 증가되는 db 내 id")
    private long idx;

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @Schema(example = "토큰에 들어갈 회원 식별 값")
    private UUID uid;

    @Column(nullable = false, unique = true, length = 30)
    @Schema(example = "회원이 로그인 시 사용할 id")
    private String loginId;

    @JsonIgnore
    @Schema(example = "회원이 로그인 시 사용할 password")
    private String loginPassword;

    @Schema(example = "회원가입 시의 이름 (카카오에서는 닉네임)")
    private String nickName;

    @Enumerated(EnumType.STRING)
    @Schema(example = "회원 role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Schema(example = "회원가입 type")
    private Type type;

    @Column(unique = true)
    @Schema(example = "회원의 refresh token")
    private String refreshToken;

    public void updateUserPassword(String password) {
        this.loginPassword = password;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
