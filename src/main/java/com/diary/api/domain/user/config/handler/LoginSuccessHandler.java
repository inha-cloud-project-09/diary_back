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

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final String secret;
    private final UserService userService;
    private final UserRepository userRepository;
    private final Environment environment;

    // 모든 사용자에게 기본 권한 부여
    private final String DEFAULT_USER_ROLE = "USER";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String loginUserEmail = null;
        User loginUser = null;

        // OAuth2 로그인 (구글)
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            String sub = (String) attributes.get("sub");
            String name = (String) attributes.get("name");

            // DB에 직접 저장하는 로직 구현
            loginUser = saveOrUpdateGoogleUser(email, sub, name);
            loginUserEmail = loginUser.getEmail();
        }
        // 이메일/비밀번호 로그인
        else if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            loginUserEmail = userPrincipal.getUsername();
            loginUser = userRepository.findByEmail(loginUserEmail)
                    .orElseThrow(() -> new IllegalStateException("User not found"));
        } else {
            log.warn("[인증 성공] 알 수 없는 Principal 타입: {}", authentication.getPrincipal());
            response.sendRedirect("http://localhost:3000?error=unknown_principal");
            return;
        }

        if (loginUser != null) {
            try {
                long expirationMillis = 360000000L;
                String jwt = jwtUtil.generateToken(loginUserEmail, loginUser.getId(), secret, expirationMillis);

                String frontendUrl = environment.getProperty("frontend.url", "http://localhost:3000");

                // 환경에 상관없이 동일한 쿠키 설정 적용
                ResponseCookie jwtCookie = ResponseCookie.from("auth-token", jwt)
                        .path("/")
                        .httpOnly(true)
                        .maxAge(Duration.ofSeconds(expirationMillis / 1000))
                        .secure(false)
                        .build();

                ResponseCookie roleCookie = ResponseCookie.from("userRole", DEFAULT_USER_ROLE)
                        .path("/")
                        .httpOnly(false)
                        .maxAge(Duration.ofSeconds(expirationMillis / 1000))
                        .secure(false)
                        .build();

                response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
                response.addHeader(HttpHeaders.SET_COOKIE, roleCookie.toString());

                // 프론트엔드로 리다이렉트
                response.sendRedirect(frontendUrl);
            } catch (Exception e) {
                log.error("[JWT 생성 실패]", e);
                response.sendRedirect("http://localhost:3000?error=jwt_error");
            }
        } else {
            response.sendRedirect("http://localhost:3000?error=no_user_found");
        }
    }

    // 구글 로그인 사용자 정보를 DB에 저장하는 메서드
    private User saveOrUpdateGoogleUser(String email, String sub, String name) {
        // googleId로 사용자 조회 (provider/providerId 대신)
        Optional<User> existingUser = userRepository.findByGoogleId(sub);

        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            // 새 사용자 생성
            return userService.createGoogleUser(email, sub, name);
        }
    }
}