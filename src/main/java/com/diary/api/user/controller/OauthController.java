package com.diary.api.user.controller;

import com.diary.api.config.UserPrincipal;
import com.diary.api.user.User;
import com.diary.api.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OauthController {
    private final UserRepository userRepository;
    private final Environment environment;

    // 기본 사용자 권한
    private final String DEFAULT_USER_ROLE = "USER";

    /**
     * 구글 로그인 리다이렉트 (프런트에서 이 경로로 요청하면, 내부적으로 /oauth2/authorization/google 로 이동)
     */
    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        // 현재 활성화된 프로필 확인
        boolean isProdProfile = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        // 환경에 따라 리다이렉트 URL 결정
        String redirectUrl = isProdProfile
                ? ""
                : "";
        response.sendRedirect(redirectUrl);
    }

    /**
     * 현재 로그인 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 인증되지 않은 사용자 처리
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다");
        }

        // UserPrincipal에서 이메일을 가져옵니다
        String email = userPrincipal.getUsername();

        // UserRepository에서 email 기반으로 유저 정보 확인
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다");
        }

        User user = userOpt.get();

        // 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("id", user.getId());

        // nickname 필드가 없으므로 이메일 사용
        result.put("nickname", email);

        // role 필드가 없으므로 기본값 사용
        result.put("role", DEFAULT_USER_ROLE);

        result.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(result);
    }
}