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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
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

    // secret 값을 @Value 어노테이션으로 주입
    @Value("${jwt.secret}")
    private String secret;

    @Value("${frontend.url}")
    private String frontendUrl;

    // 모든 사용자에게 기본 권한 부여
    private final String DEFAULT_USER_ROLE = "USER";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        User loginUser = extractLoginUser(authentication);

        if (loginUser != null) {
            try {
                long expirationMillis = 3600000L; // 1시간
                String jwt = jwtUtil.generateToken(loginUser.getEmail(), loginUser.getId(), secret, expirationMillis);

                log.info("[JWT 토큰 발급] 사용자: {}, 토큰: {}", loginUser.getEmail(), jwt);

                ResponseCookie.ResponseCookieBuilder jwtCookieBuilder = ResponseCookie.from("auth-token", jwt)
                        .path("/")
                        .httpOnly(true)
                        .maxAge(Duration.ofMillis(expirationMillis))
                        .secure(true)
                        .sameSite("None")
                        .domain("withudiary.my");

                ResponseCookie.ResponseCookieBuilder roleCookieBuilder = ResponseCookie.from("userRole", DEFAULT_USER_ROLE)
                        .path("/")
                        .httpOnly(false)
                        .maxAge(Duration.ofMillis(expirationMillis))
                        .secure(true)
                        .sameSite("None")
                        .domain("withudiary.my");

                // 쿠키 생성 및 응답에 추가
                ResponseCookie jwtCookie = jwtCookieBuilder.build();
                ResponseCookie roleCookie = roleCookieBuilder.build();

                response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
                response.addHeader(HttpHeaders.SET_COOKIE, roleCookie.toString());

                // 지정된 프론트엔드 URL로 리다이렉트
                response.sendRedirect(frontendUrl);

            } catch (Exception e) {
                log.error("[JWT 생성 또는 리다이렉션 실패]", e);
                response.sendRedirect(frontendUrl + "?error=jwt_error");
            }
        } else {
            response.sendRedirect(frontendUrl + "?error=no_user_found");
        }
    }

    private User extractLoginUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            String sub = (String) attributes.get("sub");
            String name = (String) attributes.get("name");
            return saveOrUpdateGoogleUser(email, sub, name);
        } else if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userRepository.findByEmail(userPrincipal.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
        } else {
            log.warn("[인증 성공] 알 수 없는 Principal 타입: {}", authentication.getPrincipal());
            return null;
        }
    }

    private User saveOrUpdateGoogleUser(String email, String sub, String name) {
        return userRepository.findByGoogleId(sub)
                .orElseGet(() -> userService.createGoogleUser(email, sub, name));
    }
}