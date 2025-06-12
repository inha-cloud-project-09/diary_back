package com.diary.api.controller;

import com.diary.api.common.ApiResponse;
import com.diary.api.domain.user.config.UserPrincipal;
import com.diary.api.domain.user.entity.User;
import com.diary.api.domain.user.repository.UserRepository;
import com.diary.api.domain.user.service.UserService;
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
import org.springframework.web.bind.annotation.*;
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
    private final UserService userService;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String googleAuthUri;

    // 기본 사용자 권한
    private final String DEFAULT_USER_ROLE = "USER";

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", user.getId());
                    result.put("email", user.getEmail());
                    result.put("googleId", user.getGoogleId());
                    result.put("createdAt", user.getCreatedAt());

                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "사용자를 찾을 수 없습니다");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    private String generateRandomString(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 구글 로그인 리다이렉트 (프런트에서 이 경로로 요청하면, 내부적으로 /oauth2/authorization/google 로 이동)
     */
    @GetMapping("/google")
    public void googleLoginRedirect(HttpServletResponse response) throws IOException {

        response.sendRedirect("/oauth2/authorization/google");
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