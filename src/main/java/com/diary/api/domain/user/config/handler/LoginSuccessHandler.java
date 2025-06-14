package com.diary.api.domain.user.config.handler;

import com.diary.api.domain.user.entity.User;
import com.diary.api.domain.user.repository.UserRepository;
import com.diary.api.domain.user.service.UserService;
import com.diary.api.domain.user.util.JwtUtil;
import com.diary.api.domain.user.config.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final Environment environment;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        log.info("[LoginSuccessHandler] 인증 성공 처리 시작");

        String profile = environment.getProperty("spring.profiles.active", "local");
        String frontendUrl = environment.getProperty("frontend.url", "http://localhost:3000");
        String secretKey = environment.getProperty("jwt.secret");
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret key가 설정되지 않았습니다.");
        }

        User user = extractLoginUser(authentication);
        if (user == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), secretKey, 86400000L);
        log.info("[LoginSuccessHandler] JWT 생성 완료: {}", token);

        if ("local".equals(profile)) {
            // ✅ 로컬 환경: Swagger 테스트용 JSON 응답
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", token);
            responseBody.put("userId", user.getId());
            responseBody.put("email", user.getEmail());

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(responseBody));
        } else {
            // ✅ 운영 환경: Secure 쿠키 + 리다이렉트
            ResponseCookie cookie = ResponseCookie.from("auth-token", token)
                    .path("/")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .maxAge(Duration.ofDays(1))
                    .build();


            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.sendRedirect(frontendUrl + "/dashboard");
        }
    }

    private User extractLoginUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            String sub = (String) attributes.get("sub");
            String name = (String) attributes.get("name");

            if (email == null || sub == null) {
                log.warn("OAuth2 사용자 정보 누락: email={}, sub={}", email, sub);
                return null;
            }

            return userService.upsertGoogleUser(email, sub, name);

        } else if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userRepository.findByEmail(userPrincipal.getUsername())
                    .orElse(null);
        } else {
            log.warn("[인증 성공] 알 수 없는 Principal 타입: {}", authentication.getPrincipal());
            return null;
        }
    }
}
