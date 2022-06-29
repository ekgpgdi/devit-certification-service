package com.devit.devitcertificationservice.user.controller;

import com.devit.devitcertificationservice.auth.dto.TokenDto;
import com.devit.devitcertificationservice.common.ResponseDetails;
import com.devit.devitcertificationservice.user.dto.JoinDto;
import com.devit.devitcertificationservice.user.entity.Type;
import com.devit.devitcertificationservice.user.sevice.UserService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Api(tags = "User Controller")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> join(HttpServletRequest request, HttpServletResponse response, @RequestBody JoinDto requestJoinDTO) {
        TokenDto token = userService.join(response ,requestJoinDTO, Type.GENERAL);
        ResponseDetails responseDetails;
        if (token == null) {
            responseDetails = ResponseDetails.badRequest("회원가입 실패(이메일 중복)", "/api/auth/join");
            return new ResponseEntity<>(responseDetails, HttpStatus.BAD_REQUEST);
        }
        responseDetails = ResponseDetails.created(token, "/api/auth/join");
        return new ResponseEntity<>(responseDetails, HttpStatus.CREATED);
    }

    @PostMapping("/duplicate-check")
    public ResponseEntity<?> duplicateCheck(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> requestObject) {
        Boolean duplicate = userService.duplicateCheck(requestObject);
        ResponseDetails responseDetails = ResponseDetails.success(duplicate, "/api/auth/duplicate-check");
        return new ResponseEntity<>(responseDetails, HttpStatus.CREATED);
    }

    @GetMapping("/api/auth/kakao/login")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code, HttpServletResponse response){
        // authorizedCode: 카카오 서버로부터 받은 인가 코드
        TokenDto token = userService.kakao(code, response);
        ResponseDetails responseDetails;
        if (token == null) {
            responseDetails = ResponseDetails.fail("토큰 발급에 실패했습니다.", "/api/auth/kakao/login");
            return new ResponseEntity<>(responseDetails, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        responseDetails = ResponseDetails.success(token, "/api/auth/kakao/login");
        return new ResponseEntity<>(responseDetails, HttpStatus.OK);
    }
}
