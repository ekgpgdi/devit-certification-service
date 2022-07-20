package com.devit.devitcertificationservice.auth.security;

import com.devit.devitcertificationservice.user.dto.KakaoUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

@Component
@Slf4j
public class KakaoOAuth2 {
    @Value("${kakao.secret}")
    private String restApiKey;

    public KakaoUserInfo getUserInfo(String authorizedCode) {
        log.info("인가 코드로 카카오 액세스 토큰 얻기 시작");
        // 1. 인가코드 -> 액세스 토큰
        String accessToken = getAccessToken(authorizedCode);
        log.info("액세스 토큰으로 카카오 사용자 정보 조회 시작");
        // 2. 액세스 토큰 -> 카카오 사용자 정보
        return getUserInfoByToken(accessToken);
    }

    /**
     * 인가 코드를 이용하여 kakao access token 가져오기
     */
    private String getAccessToken(String authorizedCode) {
        // HttpHeader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", restApiKey);
        params.add("redirect_uri", "https://www.devit.shop/login");
        params.add("code", authorizedCode);

        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        log.info("카카오에게 액세스 토큰 요청 시작");
        // Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        log.info("카카오에게 액세스 토큰 요청 성공");
        // JSON -> 액세스 토큰 파싱
        String tokenJson = response.getBody();
        JSONObject rjson = new JSONObject(tokenJson);

        return rjson.getString("access_token");
    }

    /**
     * 카카오 access 토큰으로 카카오 유저 정보 가져오기
     */
    private KakaoUserInfo getUserInfoByToken(String accessToken) {
        // HttpHeader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        log.info("카카오에게 사용자 정보 요청 시작");
        // Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        log.info("카카오에게 사용자 정보 요청 성공");
        JSONObject body = new JSONObject(response.getBody());
        Long id = body.getLong("id");
        String email = body.getJSONObject("kakao_account").getString("email");
        String nickname = body.getJSONObject("properties").getString("nickname");

        return new KakaoUserInfo(id, email, nickname);
    }
}