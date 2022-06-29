package com.devit.devitcertificationservice.user.sevice;

import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.auth.service.AuthService;
import com.devit.devitcertificationservice.auth.util.CookieUtil;
import com.devit.devitcertificationservice.auth.util.token.AuthToken;
import com.devit.devitcertificationservice.user.dto.JoinDto;
import com.devit.devitcertificationservice.user.entity.Type;
import com.devit.devitcertificationservice.user.entity.UserCertification;
import com.devit.devitcertificationservice.user.repository.UserCertificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserCertificationRepository userCertificationRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 이메일 중복 체크
     */
    public boolean emailDuplicate(String email){
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
        if(emailDuplicate(requestJoinDTO.getEmail())){
            return null;
        }

        AuthToken refreshToken = authService.refreshToken(user);
        user.updateRefreshToken(refreshToken.getToken());
        AuthToken accessToken = authService.AccessToken(user);

        user.updateUserPassword(passwordEncoder.encode(requestJoinDTO.getPassword()));
        authService.refreshTokenAddCookie(response, refreshToken.getToken());

        userCertificationRepository.save(user);

        return new TokenDto(accessToken.getToken(), refreshToken.getToken());
    }

    /**
     * 이메일 중복 체크
     */
    public Boolean duplicateCheck(Map<String, String> requestObject) {
        String email = requestObject.get("email");
        return emailDuplicate(email);
    }
}
