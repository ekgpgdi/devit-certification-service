package com.devit.devitcertificationservice.sevice;

import com.devit.devitcertificationservice.dto.JoinDto;
import com.devit.devitcertificationservice.dto.KakaoUserInfo;
import com.devit.devitcertificationservice.dto.LoginDto;
import com.devit.devitcertificationservice.dto.TokenDto;
import com.devit.devitcertificationservice.entity.Role;
import com.devit.devitcertificationservice.entity.Type;
import com.devit.devitcertificationservice.properties.AppProperties;
import com.devit.devitcertificationservice.rabbitMQ.RabbitMqSender;
import com.devit.devitcertificationservice.rabbitMQ.dto.UserDto;
import com.devit.devitcertificationservice.security.KakaoOAuth2;
import com.devit.devitcertificationservice.util.CookieUtil;
import com.devit.devitcertificationservice.util.HeaderUtil;
import com.devit.devitcertificationservice.token.AuthToken;
import com.devit.devitcertificationservice.token.AuthTokenProvider;
import com.devit.devitcertificationservice.common.ResponseDetails;
import com.devit.devitcertificationservice.entity.UserCertification;
import com.devit.devitcertificationservice.repository.UserCertificationRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final UserCertificationRepository userCertificationRepository;
    private final AuthTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMqSender rabbitMqSender;
    private final KakaoOAuth2 kakaoOAuth2;
    private final static long THREE_DAYS_MSEC = 259200000;

    @Value("${kakao.adminToken}")
    private String ADMIN_TOKEN;

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
     * 이메일 중복 체크
     */
    public boolean emailDuplicate(String email) {
        Optional<UserCertification> user = userCertificationRepository.findByLoginId(email);
        return user.isPresent();
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

        log.info("cookie에 refreshToken을 삭제합니다.");
        CookieUtil.deleteCookie(httpRequest, httpResponse, AuthToken.REFRESH_TOKEN, ".devit.shop");
        log.info("cookie에 refreshToken을 추가합니다.");
        refreshTokenAddCookie(httpResponse, refreshToken.getToken());

        return new TokenDto(accessToken.getToken());
    }


    /**
     * 회원가입
     */
    public TokenDto join(HttpServletRequest request, HttpServletResponse response, JoinDto requestJoinDTO, Type general) {
        log.info("회원 가입 진행");
        // uuid 생성
        UUID uuid = UUID.randomUUID();

        log.info("UserCertification entity 생성");
        UserCertification user = UserCertification.builder()
                .loginId(requestJoinDTO.getEmail())
                .nickName(requestJoinDTO.getNickName())
                .role(Role.valueOf("GENERAL"))
                .type(general)
                .uid(uuid)
                .build();

        // 이메일이 중복되는 경우
        if (emailDuplicate(requestJoinDTO.getEmail())) {
            return null;
        }

        log.info("refresh token 생성");
        AuthToken refreshToken = refreshToken(user);
        log.info("UserCertification entity refresh token 업데이트");
        user.updateRefreshToken(refreshToken.getToken());
        log.info("access token 생성");
        AuthToken accessToken = AccessToken(user);

        log.info("UserCertification entity password 업데이트");
        user.updateUserPassword(passwordEncoder.encode(requestJoinDTO.getPassword()));

        log.info("refresh token cookie 삭제");
        CookieUtil.deleteCookie(request, response, AuthToken.REFRESH_TOKEN, ".devit.shop");
        log.info("refresh token cookie 추가");
        refreshTokenAddCookie(response, refreshToken.getToken());

        log.info("user 를 테이블에 저장합니다.");
        userCertificationRepository.save(user);

        UserDto userDto = new UserDto(requestJoinDTO.getEmail(), requestJoinDTO.getNickName(), uuid);

        log.info("메세지큐 통신을 요청합니다");
        rabbitMqSender.send(userDto);

        return new TokenDto(accessToken.getToken());
    }

    /**
     * 카카오 로그인 시 DB에 회원 정보가 있다면 토큰 발급
     */
    public TokenDto getToken(UserCertification user, HttpServletResponse response) {
        log.info("email : {} 유저의 refreshToken 을 발급합니다.", user.getLoginId());
        // refresh token 발급 및 쿠키에 저장
        AuthToken refreshToken = refreshToken(user);
        user.updateRefreshToken(refreshToken.getToken());
        log.info("email : {} 유저의 refreshToken 을 쿠키에 저장합니다. [refreshToken:{}]", user.getLoginId(), refreshToken.getToken());
        refreshTokenAddCookie(response, refreshToken.getToken());

        log.info("email : {} 유저의 accessToken 을 발급합니다.", user.getLoginId());
        // access token 발급
        AuthToken accessToken = AccessToken(user);

        return new TokenDto(accessToken.getToken());
    }

    /**
     * 카카오 로그인 시 DB에 회원 정보가 있다면 비밀번호 체크 로직 실행
     */
    public TokenDto kakaoLogin(KakaoUserInfo userInfo, UserCertification user, HttpServletResponse response) {
        log.info("기존 카카오 로그인 유저의 로그인을 진행합니다. [id : {}]", userInfo.getId());
        Long kakaoId = userInfo.getId();
        // 패스워드를 카카오 Id + ADMIN TOKEN 로 지정
        String password = kakaoId + ADMIN_TOKEN;
        if (!passwordEncoder.matches(password, user.getLoginPassword())) {
            log.error("기존 카카오 로그인 유저의 패스워드가 일치하지 않습니다. [id : {}]", userInfo.getId());
            return null;
        }
        return getToken(user, response);
    }

    /**
     * 카카오 로그인 시 DB에 회원 정보가 없다면 회원가입 로직 실행
     */
    public TokenDto kakaoJoin(HttpServletRequest request, KakaoUserInfo userInfo, HttpServletResponse response) {
        log.info("카카오 유저 id : {}", userInfo.getId());
        Long kakaoId = userInfo.getId();
        // 패스워드를 카카오 Id + ADMIN TOKEN 로 지정
        String password = kakaoId + ADMIN_TOKEN;
        log.info("새로운 joinDto 생성");
        JoinDto joinDto = new JoinDto(userInfo.getEmail(), password, userInfo.getNickname());
        return join(request, response, joinDto, Type.valueOf("SOCIAL"));
    }

    /**
     * 프론트에서 카카오 로그인 클릭 시 실행되는 서비스 로직
     */
    public TokenDto kakao(String authorizedCode, HttpServletResponse response, HttpServletRequest request) {
        log.info("카카오 OAuth2 를 통해 카카오 사용자 정보 조회 시작");
        // 카카오 OAuth2 를 통해 카카오 사용자 정보 조회
        KakaoUserInfo userInfo = kakaoOAuth2.getUserInfo(authorizedCode);
        log.info("카카오 OAuth2 를 통해 카카오 사용자 정보 조회 완료");

        log.info("이메일 중복 체크 진행");
        log.info("중복 체크 진행 이메일 : {} ", userInfo.getEmail());
        // 이메일 중복 체크 진행 → 중복이라면 이미 가입된 유저임
        Optional<UserCertification> user = userCertificationRepository.findByLoginId(userInfo.getEmail());
        log.info("이메일 중복 체크 진행 완료");
        TokenDto token;
        if (user.isPresent()) {
            log.info("이미 존재하는 회원 토큰 발급 [email: {}]", userInfo.getEmail());
            // 이미 존재하는 회원이면 토큰 발급
            token = kakaoLogin(userInfo, user.get(), response);
        } else {
            log.info("새로운 회원 토큰 발급[email: {}]", userInfo.getEmail());
            token = kakaoJoin(request, userInfo, response);
        }
        return token;
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

        if (!authRefreshToken.validate()) {
            log.info("쿠키에 refreshToken이 유효하지 않습니다.");
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
