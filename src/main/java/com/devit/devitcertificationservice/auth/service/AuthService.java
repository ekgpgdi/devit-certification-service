package com.devit.devitcertificationservice.auth.service;

import com.devit.devitcertificationservice.auth.dto.LoginDto;
import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.auth.properties.AppProperties;
import com.devit.devitcertificationservice.auth.util.CookieUtil;
import com.devit.devitcertificationservice.auth.util.HeaderUtil;
import com.devit.devitcertificationservice.auth.util.token.AuthToken;
import com.devit.devitcertificationservice.auth.util.token.AuthTokenProvider;
import com.devit.devitcertificationservice.common.ResponseDetails;
import com.devit.devitcertificationservice.user.entity.Role;
import com.devit.devitcertificationservice.user.entity.UserCertification;
import com.devit.devitcertificationservice.user.repository.UserCertificationRepository;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        Date now = new Date();
        long refreshTokenExpiry = appProperties.getRefreshTokenExpiry();

        AuthToken refreshToken = tokenProvider.createAuthToken(new Date(now.getTime() + refreshTokenExpiry));
        user.updateRefreshToken(refreshToken.getToken());

        return refreshToken;
    }

    /**
     * access token 발급
     */
    public AuthToken AccessToken(UserCertification user) {
        Date now = new Date();
        return tokenProvider.createAuthToken(
                user.getLoginId(),
                user.getUid(),
                user.getRole().getCode(),
                new Date(now.getTime() + appProperties.getTokenExpiry())
        );
    }

    /**
     * login
     */
    public TokenDto login(HttpServletRequest httpRequest, HttpServletResponse httpResponse, LoginDto requestLoginDTO) {
        Optional<UserCertification> userCertificationOptional = userCertificationRepository.findByLoginId(requestLoginDTO.getEmail());
        if (userCertificationOptional.isEmpty()) {
            System.out.println("이메일로 회원 못찾음");
            return null;
        }

        UserCertification user = userCertificationOptional.get();

        if (!passwordEncoder.matches(requestLoginDTO.getPassword(), user.getLoginPassword())) {
            System.out.println("비밀번호 맞지 않음");
            return null;
        }

        AuthToken refreshToken = refreshToken(user);
        AuthToken accessToken = AccessToken(user);

        return new TokenDto(accessToken.getToken(), refreshToken.getToken());
    }

    public void refreshTokenAddCookie(HttpServletResponse response, String refreshToken) {
        long refreshTokenExpiry = appProperties.getRefreshTokenExpiry();
        int cookieMaxAge = (int) refreshTokenExpiry / 60;
        CookieUtil.addCookie(response, AuthToken.REFRESH_TOKEN, refreshToken, cookieMaxAge, "localhost");
    }

    /**
     * 토큰 재발급
     */
    public ResponseDetails refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // access token 확인
        String accessToken = HeaderUtil.getAccessToken(request);
        AuthToken authToken = tokenProvider.convertAuthToken(accessToken);

        String path = "/api/auth/refresh";

        System.out.println(authToken.getToken());
        if (!authToken.validate()) {
            return ResponseDetails.invalidAccessToken(path);
        }

        // expired access token 인지 확인
        Claims claims = authToken.getExpiredTokenClaims();
        if (claims == null) {
            return ResponseDetails.notExpiredTokenYet(path);
        }

        String email = (String)claims.get(AuthToken.USER_ID);

        // refresh token
        String refreshToken = CookieUtil.getCookie(request, AuthToken.REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElse((null));
        AuthToken authRefreshToken = tokenProvider.convertAuthToken(refreshToken);

        if (authRefreshToken.validate()) {
            return ResponseDetails.invalidRefreshToken(path);
        }

        // userId refresh token 으로 DB 확인
        UserCertification user = userCertificationRepository.findByLoginId(email).orElseThrow(
                () -> new NullPointerException("아이디가 존재하지 않습니다.")
        );

        String userRefreshToken = user.getRefreshToken();
        if (!userRefreshToken.equals(refreshToken)) {
            return ResponseDetails.invalidRefreshToken(path);
        }

        Date now = new Date();
        AuthToken newAccessToken = tokenProvider.createAuthToken(
                user.getLoginId(),
                user.getUid(),
                user.getRole().getCode(),
                new Date(now.getTime() + appProperties.getTokenExpiry())
        );

        long validTime = authRefreshToken.getTokenClaims().getExpiration().getTime() - now.getTime();

        // refresh 토큰 기간이 3일 이하로 남은 경우, refresh 토큰 갱신
        if (validTime >= THREE_DAYS_MSEC) {
            // refresh 토큰 설정
            long refreshTokenExpiry = appProperties.getRefreshTokenExpiry();

            authRefreshToken = tokenProvider.createAuthToken(new Date(now.getTime() + refreshTokenExpiry));

            // DB에 refresh 토큰 업데이트
            user.updateRefreshToken(authRefreshToken.getToken());

            CookieUtil.deleteCookie(request, response, AuthToken.REFRESH_TOKEN, "localhost");
            refreshTokenAddCookie(response, authRefreshToken.getToken());
        }

        return ResponseDetails.success(new TokenDto(newAccessToken.getToken(), authRefreshToken.getToken()), path);
    }
}
