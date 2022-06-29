package com.devit.devitcertificationservice.user.entity;

import com.devit.devitcertificationservice.auth.util.Timestamped;
import com.devit.devitcertificationservice.user.dto.JoinDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCertification extends Timestamped {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(example = "자동 증가되는 db 내 id")
    private long idx;

    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    @ApiModelProperty(example = "토큰에 들어갈 회원 식별 값")
    private UUID uid;

    @Column(nullable = false, unique = true, length = 30)
    @ApiModelProperty(example = "회원이 로그인 시 사용할 id")
    private String loginId;

    @JsonIgnore
    @ApiModelProperty(example = "회원이 로그인 시 사용할 password")
    private String loginPassword;

    @Enumerated(EnumType.STRING)
    @ApiModelProperty(example = "회원 role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @ApiModelProperty(example = "회원가입 type")
    private Type type;

    @Column(unique = true)
    @ApiModelProperty(example = "회원의 refresh token")
    private String refreshToken;

    public UserCertification(JoinDto requestJoinDTO, Type general, UUID uid) {
        this.loginId = requestJoinDTO.getEmail();
        this.role = Role.valueOf(requestJoinDTO.getRole());
        this.type = general;
        this.uid = uid;
    }

    public void updateUserPassword(String password) {
        this.loginPassword = password;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
