package com.devit.devitcertificationservice.sevice;

import com.devit.devitcertificationservice.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VerificationCodeMailService implements VerificationCodeService {
    public final SimpleMailMessage template;
    public final EmailService emailService;

    /**
     * 6자리의 난수 생성
     */
    public int makeRandomNumber() {
        log.info("6자리 난수 생성");
        // 난수의 범위 111111 ~ 999999 (6자리 난수)
        Random r = new Random();
        int checkNum = r.nextInt(888888) + 111111;
        log.info("인증번호 : " + checkNum);
        return checkNum;
    }


    /**
     * 이메일 전송
     */
    public String send(String email) {
        log.info("이메일 전송 양식 구성");
        int code = makeRandomNumber();
        String text = String.format(Objects.requireNonNull(template.getText()), code);
        String subject = "[회원 가입 인증 이메일] DevIT";
        log.info("이메일 전송 시작 [이메일 :{}, 코드: {}]", email, code);
        emailService.sendSimpleMessage(email, subject, text);
        return Integer.toString(code);
    }

}
