package com.devit.devitcertificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Schema(description = "token 응답 DTO")
public class TokenDto {
    @Schema(description = "JWT 기반의 access token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNjU3MTU4ODM1LCJyb2xlIjoiR0VORVJBTCIsInVpZCI6IjlmMjdiNzQ5LWJkOTktNDBmZC05MzgzLTY4ZmI4YTllODk2NiIsImVtYWlsIjoiZGxla2dwMDQyM0BuYXZlci5jb20iLCJuaWNrTmFtZSI6IuydtOuLpO2YnCIsImV4cCI6MTY1NzE2OTYzNX0.D1ET1NqwvMmFjozw8mTCJ8MQcRSlqVIEI5nbfBvTezI")
    private final Object accessToken;
}
