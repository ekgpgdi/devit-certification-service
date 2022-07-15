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
        // uuid 생성
        UUID uuid = UUID.randomUUID();

        UserCertification user = new UserCertification(requestJoinDTO, general, uuid);
        // 이메일이 중복되는 경우
        if (emailDuplicate(requestJoinDTO.getEmail())) {
            return null;
        }

        AuthToken refreshToken = authService.refreshToken(user);
        user.updateRefreshToken(refreshToken.getToken());
        AuthToken accessToken = authService.AccessToken(user);

        user.updateUserPassword(passwordEncoder.encode(requestJoinDTO.getPassword()));
        authService.refreshTokenAddCookie(response, refreshToken.getToken());

        userCertificationRepository.save(user);

        UserDto userDto = new UserDto(requestJoinDTO.getEmail(), requestJoinDTO.getNickName(), uuid);

        rabbitMqSender.send(userDto);

        return new TokenDto(accessToken.getToken());
    }

    /**
     * 카카오 로그인 시 DB에 회원 정보가 있다면 토큰 발급
     */
    public TokenDto getToken(UserCertification user, HttpServletResponse response) {
        // refresh token 발급 및 쿠키에 저장
        AuthToken refreshToken = authService.refreshToken(user);
        user.updateRefreshToken(refreshToken.getToken());
        authService.refreshTokenAddCookie(response, refreshToken.getToken());

        // access token 발급
        AuthToken accessToken = authService.AccessToken(user);

        return new TokenDto(accessToken.getToken());
    }

    /**
     * 카카오 로그인 시 DB에 회원 정보가 있다면 비밀번호 체크 로직 실행
     */
    public TokenDto kakaoLogin(KakaoUserInfo userInfo, UserCertification user, HttpServletResponse response) {
        Long kakaoId = userInfo.getId();
        // 패스워드를 카카오 Id + ADMIN TOKEN 로 지정
        String password = kakaoId + ADMIN_TOKEN;
        if (!passwordEncoder.matches(password, user.getLoginPassword())) {
            return null;
        }
        return getToken(user, response);
    }

    /**
     * 카카오 로그인 시 DB에 회원 정보가 없다면 회원가입 로직 실행
     */
    public TokenDto kakaoJoin(KakaoUserInfo userInfo, HttpServletResponse response) {
        Long kakaoId = userInfo.getId();
        // 패스워드를 카카오 Id + ADMIN TOKEN 로 지정
        String password = kakaoId + ADMIN_TOKEN;
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

        // 이메일 중복 체크 진행 → 중복이라면 이미 가입된 유저임
        Optional<UserCertification> user = userCertificationRepository.findByLoginId(userInfo.getEmail());
        TokenDto token;
        if (user.isPresent()) {
            // 이미 존재하는 회원이면 토큰 발급
            token = kakaoLogin(userInfo, user.get(), response);
        } else {
            token = kakaoJoin(userInfo, response);
        }
        return token;
    }
}
