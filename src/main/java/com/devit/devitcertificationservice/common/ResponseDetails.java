package com.devit.devitcertificationservice.common;

import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.rabbitMQ.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ResponseDetails {
    private final static int SUCCESS = 200;
    private final static int CREATED = 201;
    private final static int INVALID_REFRESH_TOKEN_CODE = 202;
    private final static int NOT_EXPIRED_TOKEN_YET_CODE = 203;
    private final static int BAD_REQUEST = 400;
    private final static int NOT_FOUND = 404;
    private final static int UNAUTHORIZED = 401;
    private final static int FAILED = 500;

    private final static String SUCCESS_MESSAGE = "SUCCESS";
    private final static String NOT_FOUND_MESSAGE = "NOT FOUND";
    private final static String FAILED_MESSAGE = "Server error detected.";
    private final static String INVALID_ACCESS_TOKEN_MESSAGE = "Invalid access token.";
    private final static String INVALID_REFRESH_TOKEN_MESSAGE = "Invalid refresh token.";
    private final static String NOT_EXPIRED_TOKEN_YET_MESSAGE = "Not expired token yet.";

    @Schema
    private Date timestamp;
    @Schema(anyOf = { TokenDto.class, UserDto.class, String.class })
    private Object data;
    @Schema
    private int httpStatus;
    @Schema
    private String path;

    public ResponseDetails(Object data, int httpStatus, String path){
        this.timestamp = new Date();
        this.data = data;
        this.httpStatus = httpStatus;
        this.path = path;
    }

    public static ResponseDetails success(Object data, String path) {
        return new ResponseDetails(data, SUCCESS, path);
    }

    public static ResponseDetails created(Object data, String path) {
        return new ResponseDetails(data, CREATED, path);
    }

    public static ResponseDetails fail(Object data ,String path) {
        return new ResponseDetails(data, FAILED, path);
    }

    public static ResponseDetails loginFail(Object data ,String path) {
        return new ResponseDetails(data, UNAUTHORIZED, path);
    }

    public static ResponseDetails badRequest(Object data ,String path) {
        return new ResponseDetails(data, BAD_REQUEST, path);
    }

    public static ResponseDetails notFound(Object data, String path) {
        return new ResponseDetails(data, NOT_FOUND, path);
    }

    public static ResponseDetails invalidAccessToken(String path) {
        return new ResponseDetails(INVALID_ACCESS_TOKEN_MESSAGE, FAILED, path);
    }

    public static ResponseDetails invalidRefreshToken(String path) {
        return new ResponseDetails(INVALID_REFRESH_TOKEN_MESSAGE, INVALID_REFRESH_TOKEN_CODE, path);
    }

    public static ResponseDetails notExpiredTokenYet(String path) {
        return new ResponseDetails(NOT_EXPIRED_TOKEN_YET_MESSAGE, NOT_EXPIRED_TOKEN_YET_CODE, path);
    }
}
