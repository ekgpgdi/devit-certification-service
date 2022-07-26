package com.devit.devitcertificationservice.auth.service;

import com.devit.devitcertificationservice.auth.dto.LoginDto;
import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.auth.properties.AppProperties;
import com.devit.devitcertificationservice.auth.util.CookieUtil;
import com.devit.devitcertificationservice.auth.util.HeaderUtil;
import com.devit.devitcertificationservice.auth.util.token.AuthToken;
import com.devit.devitcertificationservice.auth.util.token.AuthTokenProvider;
import com.devit.devitcertificationservice.common.ResponseDetails;
import com.devit.devitcertificationservice.user.entity.UserCertification;
import com.devit.devitcertificationservice.user.repository.UserCertificationRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final UserCertificationRepository userCertificationRepository;
    private final AuthTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final static long THREE_DAYS_MSEC = 259200000;

    /**
     * refresh token 발급 및 userCertification update
     */
    public AuthToken refreshToken(UserCertification user) {
        log.info("--refreshToken 생성 시작 --");
        Date now = new Date();
        long refreshTokenExpiry = appProperties.getRefreshTokenExpiry();

        AuthToken refreshToken = tokenProvider.createAuthToken(user.getLoginId(), new Date(now.getTime() + refreshTokenExpiry));
        log.info("refreshToken 생성이 완료되어 user refreshToken 을 업데이트합니다.");
        user.updateRefreshToken(refreshToken.getToken());

        return refreshToken;
    }

    /**
     * access token 발급
     */
    public AuthToken AccessToken(UserCertification user) {
        log.info("--accessToken 생성 시작 --");
        Date now = new Date();
        return tokenProvider.createAuthToken(
                user.getLoginId(),
                user.getNickName(),
                user.getUid(),
                user.getRole().getCode(),
                user.getType().getCode(),
                new Date(now.getTime() + appProperties.getTokenExpiry())
        );
    }

    /**
     * login
     */
    public TokenDto login(HttpServletRequest httpRequest, HttpServletResponse httpResponse, LoginDto requestLoginDTO) {
        Optional<UserCertification> userCertificationOptional = userCertificationRepository.findByLoginId(requestLoginDTO.getEmail());
        if (userCertificationOptional.isEmpty()) {
            log.info("email : {} 유저를 찾을 수 없습니다. ", requestLoginDTO.getEmail());
            return null;
        }

        UserCertification user = userCertificationOptional.get();
        log.info("email : {} 유저를 찾았습니다. userUid : {} ", requestLoginDTO.getEmail(), user.getUid());

        if (!passwordEncoder.matches(requestLoginDTO.getPassword(), user.getLoginPassword())) {
            log.info("비밀번호가 일치하지 않습니다.");
            return null;
        }

        AuthToken refreshToken = refreshToken(user);
        log.info("refreshToken 이 생성되었습니다. refreshToken : {}", refreshToken.getToken());
        AuthToken accessToken = AccessToken(user);
        log.info("accessToken 이 생성되었습니다. accessToken : {}", accessToken.getToken());

        log.info("cookie에 refreshToken을 추가합니다.");
        refreshTokenAddCookie(httpResponse, refreshToken.getToken());

        return new TokenDto(accessToken.getToken());
    }

    /**
     * 헤더에 refresh token 추가
     */
    public void refreshTokenAddCookie(HttpServletResponse response, String refreshToken) {
        long refreshTokenExpiry = appProperties.getRefreshTokenExpiry();
        int cookieMaxAge = (int) refreshTokenExpiry / 60;
        log.info("CookieUtil의 addCookie를 요청합니다.");
        CookieUtil.addCookie(response, AuthToken.REFRESH_TOKEN, refreshToken, cookieMaxAge, ".devit.shop");
    }

    /**
     * 토큰 재발급
     */
    public ResponseDetails refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // access token 확인
        String accessToken = HeaderUtil.getAccessToken(request);
        log.info("헤더의 토큰을 확인합니다. {}", accessToken);
        AuthToken authToken = tokenProvider.convertAuthToken(accessToken);
        log.info("헤더의 토큰을 이용하여 AuthToken 을 생성합니다. {}", authToken.getToken());

        String path = "/api/auth/refresh";

        if (!authToken.validate()) {
            log.info("토큰이 유효하지 않습니다.");
            return ResponseDetails.invalidAccessToken(path);
        }

        // expired access token 인지 확인
        Claims claims = authToken.getExpiredTokenClaims();
        if (claims == null) {
            log.info("accessToken 의 만료 시간이 만료되지 않았습니다.");
            return ResponseDetails.notExpiredTokenYet(path);
        }

        String email = (String) claims.get(AuthToken.USER_ID);

        // refresh token
        String refreshToken = CookieUtil.getCookie(request, AuthToken.REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElse((null));
        log.info("refreshToken 갱신, cookie 내 refreshToken 확인 : {}", refreshToken);
        AuthToken authRefreshToken = tokenProvider.convertAuthToken(refreshToken);

        if (authRefreshToken.validate()) {
            log.info("쿠키에 refreshToken이 존재하지 않습니다.");
            return ResponseDetails.invalidRefreshToken(path);
        }

        // userId refresh token 으로 DB 확인
        UserCertification user = userCertificationRepository.findByLoginId(email).orElseThrow(
                () -> new NullPointerException("아이디가 존재하지 않습니다.")
        );

        String userRefreshToken = user.getRefreshToken();
        if (!userRefreshToken.equals(refreshToken)) {
            log.info("email : {} 유저의 refreshToken 이 일치하지 않습니다.", email);
            log.info("들어온 refreshToken : {}, 유저의 refreshToken : {}", refreshToken, userRefreshToken);
            return ResponseDetails.invalidRefreshToken(path);
        }

        Date now = new Date();
        log.info("email : {} 유저의 accessToken을 새로 생성합니다.", email);
        AuthToken newAccessToken = tokenProvider.createAuthToken(
                user.getLoginId(),
                user.getNickName(),
                user.getUid(),
                user.getRole().getCode(),
                user.getType().getCode(),
                new Date(now.getTime() + appProperties.getTokenExpiry())
        );

        long validTime = authRefreshToken.getTokenClaims().getExpiration().getTime() - now.getTime();

        // refresh 토큰 기간이 3일 이하로 남은 경우, refresh 토큰 갱신
        if (validTime >= THREE_DAYS_MSEC) {
            log.info("email : {} 유저의 refresh 토큰 기간이 3일 이하로 남아 refresh 토큰을 갱신합니다.", email);
            // refresh 토큰 설정
            long refreshTokenExpiry = appProperties.getRefreshTokenExpiry();

            authRefreshToken = tokenProvider.createAuthToken(user.getLoginId(), new Date(now.getTime() + refreshTokenExpiry));

            // DB에 refresh 토큰 업데이트
            user.updateRefreshToken(authRefreshToken.getToken());

            log.info("이전 refreshToken을 헤더에서 삭제하고 새로운 refreshToken을 추가합니다.");
            CookieUtil.deleteCookie(request, response, AuthToken.REFRESH_TOKEN, ".devit.shop");
            refreshTokenAddCookie(response, authRefreshToken.getToken());
        }

        return ResponseDetails.success(new TokenDto(newAccessToken.getToken()), path);
    }
}
