package com.devit.devitcertificationservice.rabbitMQ.dto;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(example = "회원 이메일")
    private String email;
    @ApiModelProperty(example = "회원 닉네임")
    private String nickName;
    @ApiModelProperty(example = "회원 uuid (토큰 통신에 사용 될)")
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