package com.devit.devitcertificationservice.user.sevice;

import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.auth.security.KakaoOAuth2;
import com.devit.devitcertificationservice.auth.service.AuthService;
import com.devit.devitcertificationservice.auth.util.token.AuthToken;
import com.devit.devitcertificationservice.rabbitMQ.RabbitMqSender;
import com.devit.devitcertificationservice.rabbitMQ.dto.UserDto;
import com.devit.devitcertificationservice.user.dto.JoinDto;
import com.devit.devitcertificationservice.user.dto.KakaoUserInfo;
import com.devit.devitcertificationservice.user.entity.Type;
import com.devit.devitcertificationservice.user.entity.UserCertification;
import com.devit.devitcertificationservice.user.repository.UserCertificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final UserCertificationRepository userCertificationRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMqSender rabbitMqSender;
    private final KakaoOAuth2 kakaoOAuth2;

    @Value("${kakao.adminToken}")
    private String ADMIN_TOKEN;

    /**
     * 이메일 중복 체크
     */
    public boolean emailDuplicate(String email) {
        Optional<UserCertification> user = userCertificationRepository.findByLoginId(email);
        return user.isPresent();
    }

    /**
     * 회원가입
     */
    public TokenDto join(HttpServletResponse response, JoinDto requestJoinDTO, Type general) {
        log.info("회원 가입 진행");
        // uuid 생성
        UUID uuid = UUID.randomUUID();

        log.info("UserCertification entity 생성");
        UserCertification user = new UserCertification(requestJoinDTO, general, uuid);
        // 이메일이 중복되는 경우
        if (emailDuplicate(requestJoinDTO.getEmail())) {
            return null;
        }

        log.info("refresh token 생성");
        AuthToken refreshToken = authService.refreshToken(user);
        log.info("UserCertification entity refresh token 업데이트");
        user.updateRefreshToken(refreshToken.getToken());
        log.info("access token 생성");
        AuthToken accessToken = authService.AccessToken(user);

        log.info("UserCertification entity password 업데이트");
        user.updateUserPassword(passwordEncoder.encode(requestJoinDTO.getPassword()));
        log.info("refresh token cookie 추가");
        authService.refreshTokenAddCookie(response, refreshToken.getToken());

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
        AuthToken refreshToken = authService.refreshToken(user);
        user.updateRefreshToken(refreshToken.getToken());
        log.info("email : {} 유저의 refreshToken 을 쿠키에 저장합니다. [refreshToken:{}]", user.getLoginId(), refreshToken.getToken());
        authService.refreshTokenAddCookie(response, refreshToken.getToken());

        log.info("email : {} 유저의 accessToken 을 발급합니다.", user.getLoginId());
        // access token 발급
        AuthToken accessToken = authService.AccessToken(user);

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
    public TokenDto kakaoJoin(KakaoUserInfo userInfo, HttpServletResponse response) {
        log.info("카카오 유저 id : {}", userInfo.getId());
        Long kakaoId = userInfo.getId();
        // 패스워드를 카카오 Id + ADMIN TOKEN 로 지정
        String password = kakaoId + ADMIN_TOKEN;
        log.info("새로운 joinDto 생성");
        JoinDto joinDto = new JoinDto(userInfo.getEmail(), password, userInfo.getNickname());
        return join(response, joinDto, Type.valueOf("SOCIAL"));
    }

    /**
     * 프론트에서 카카오 로그인 클릭 시 실행되는 서비스 로직
     */
    public TokenDto kakao(String authorizedCode, HttpServletResponse response) {
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
            token = kakaoJoin(userInfo, response);
        }
        return token;
    }
}
