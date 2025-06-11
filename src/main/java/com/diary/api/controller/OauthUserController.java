package com.diary.api.controller;

import com.diary.api.common.ApiResponse;
import com.diary.api.domain.user.config.UserPrincipal;
import com.diary.api.domain.user.entity.User;
import com.diary.api.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OauthUserController {
    private final UserRepository userRepository;
    private final Environment environment;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String googleAuthUri;

    // 기본 사용자 권한
    private final String DEFAULT_USER_ROLE = "USER";

    private String generateRandomString(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 구글 로그인 리다이렉트 (프런트에서 이 경로로 요청하면, 내부적으로 /oauth2/authorization/google 로 이동)
     */
    @GetMapping("/google")
    public ResponseEntity<ApiResponse<Map<String, String>>> googleLogin(
            @RequestParam(required = false) String error) {

        if (error != null) {
            log.error("Google 로그인 에러: {}", error);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Google 로그인 실패: " + error));
        }

        // 환경에 따른 리다이렉트 URL 설정
        String baseUrl = environment.getProperty("server.base-url", "http://localhost:8080");
        String redirectUri = baseUrl + "/login/oauth2/code/google";

        // 랜덤한 state와 nonce 생성
        String state = generateRandomString(32);
        String nonce = generateRandomString(32);

        String authUrl = UriComponentsBuilder.fromUriString(googleAuthUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", googleClientId)
                .queryParam("scope", "openid profile email")
                .queryParam("state", state)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("nonce", nonce)
                .build()
                .toUriString();

        log.info("Google login URL generated - baseUrl: {}, state: {}, nonce: {}", baseUrl, state, nonce);

        // 프론트엔드에서 처리할 수 있도록 전체 URL 반환
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "url", authUrl,
                "state", state,
                "nonce", nonce,
                "type", "oauth2")));
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