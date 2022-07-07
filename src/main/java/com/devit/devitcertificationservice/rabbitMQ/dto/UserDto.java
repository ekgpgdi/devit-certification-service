package com.devit.devitcertificationservice.rabbitMQ.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

/**
 * 회원가입 후 메시지로 교환할 객체의 도메인 클래스.
 */
@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
// @JsonIdentityInfo 어노테이션은 직렬화/역직렬화 중에 개체 ID가 사용됨을 나타내는 데 사용
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = UserDto.class)
public class UserDto implements Serializable {
    @Schema(description = "로그인 시 사용될 이메일", example = "dlekgp0423@naver.com")
    private String email;
    @Schema(description = "사용자 닉네임", example = "이다혜")
    private String nickName;
    @Schema(description = "사용자를 식별할 uuid", example = "9f27b749-bd99-40fd-9383-68fb8a9e8966")
    private UUID uuid;

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", name='" + nickName + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}