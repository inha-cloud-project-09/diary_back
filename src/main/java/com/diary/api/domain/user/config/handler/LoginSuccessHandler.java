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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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


    // 모든 사용자에게 기본 권한 부여
    private final String DEFAULT_USER_ROLE = "USER";
    // LoginSuccessHandler.java

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 메서드 내에서 Environment를 통해 직접 설정 값을 가져옵니다.
        String secretKey = environment.getProperty("jwt.secret");
        String frontendUrl = environment.getProperty("frontend.url"); // application.yml의 frontend.url

        // 설정 값 로드 실패 시 안전하게 처리
        if (secretKey == null || secretKey.isBlank() || frontendUrl == null) {
            log.error("### CRITICAL: 'jwt.secret' 또는 'frontend.url' 설정 값을 application.yml에서 찾을 수 없습니다! ###");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server configuration error");
            return;
        }

        User loginUser = extractLoginUser(authentication);

        if (loginUser != null) {
            try {
                long expirationMillis = 3600000L; // 1시간
                String jwt = jwtUtil.generateToken(loginUser.getEmail(), loginUser.getId(), secretKey, expirationMillis);

                ResponseCookie jwtCookie = ResponseCookie.from("auth-token", jwt)
                        .path("/")
                        .httpOnly(true)
                        .secure(true)                 // HTTPS 환경에서만 쿠키 전송 true
                        .sameSite("None")             // 크로스-사이트 요청 허용 (secure: true 필요)
                        .domain("withudiary.my")      // 쿠키가 유효한 도메인 설정 withudiary.my
                        .maxAge(Duration.ofMillis(expirationMillis))
                        .build();

                ResponseCookie roleCookie = ResponseCookie.from("userRole", DEFAULT_USER_ROLE)
                        .path("/")
                        .httpOnly(false)
                        .secure(true)                 // HTTPS 환경에서만 쿠키 전송
                        .sameSite("None")             // 크로스-사이트 요청 허용 (secure: true 필요)
                        .domain("withudiary.my")      // 쿠키가 유효한 도메인 설정
                        .maxAge(Duration.ofMillis(expirationMillis))
                        .build();

                // 쿠키를 응답 헤더에 추가
                response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
                response.addHeader(HttpHeaders.SET_COOKIE, roleCookie.toString());

                // 프론트엔드 URL로 리다이렉트
                response.sendRedirect(frontendUrl+"/dashboard");

            } catch (Exception e) {
                log.error("[JWT 생성 또는 리다이렉션 실패]", e);
                String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                        .path("/error").queryParam("code", "jwt_error").build().toUriString();
                response.sendRedirect(errorUrl);
            }
        } else {
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/error").queryParam("code", "no_user_found").build().toUriString();
            response.sendRedirect(errorUrl);
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