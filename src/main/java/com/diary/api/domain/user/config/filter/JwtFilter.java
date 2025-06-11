package com.diary.api.domain.user.config.filter;

import com.diary.api.domain.user.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 쿠키/헤더 검증 필터
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final String secretKey;
    private final UserDetailsService userDetailsService;

    // 인증이 필요 없는 경로들
    private final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/login/oauth2",
            "/oauth2/",
            "/api/auth/google",
            "/h2-console",
            "/swagger-ui",
            "/v3/api-docs");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.debug("[JwtFilter] 요청 URI: {}", requestUri);
        log.debug("[JwtFilter] 요청 메서드: {}", request.getMethod());
        log.debug("[JwtFilter] 모든 요청 헤더: {}", Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        request::getHeader)));

        // 인증이 필요 없는 경로 확인
        if (shouldSkipFilter(requestUri)) {
            log.debug("[JwtFilter] 인증이 필요 없는 경로: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // JWT 토큰 추출 시도
            String token = extractToken(request);
            // 토큰이 없으면 인증 실패 응답
            if (token == null) {
                log.debug("[JwtFilter] JWT가 없습니다. URI={}", requestUri);
                sendUnauthorizedResponse(response, "인증 토큰이 필요합니다.");
                return;
            }

            // 토큰 검증 및 인증 처리
            if (!processToken(token, request)) {
                log.debug("[JwtFilter] 토큰 검증 실패");
                sendUnauthorizedResponse(response, "인증에 실패했습니다.");
                return;
            }

        } catch (Exception e) {
            // 예외 상세 정보 로깅
            log.error("[JwtFilter] JWT 처리 중 예외 발생: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            sendUnauthorizedResponse(response, "인증 처리 중 오류가 발생했습니다.");
            return;
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    /**
     * 경로가 인증 제외 대상인지 확인
     */
    private boolean shouldSkipFilter(String requestUri) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(requestUri::startsWith);
    }

    /**
     * 요청에서 JWT 토큰 추출 (쿠키 또는 헤더)
     */
    private String extractToken(HttpServletRequest request) {
        // 쿠키에서 토큰 추출 시도
        String token = getTokenFromCookie(request);

        // 쿠키에 없으면 헤더에서 추출 시도
        if (token == null) {
            token = getTokenFromHeader(request);
        }

        return token;
    }

    /**
     * 쿠키에서 토큰 추출
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        for (Cookie cookie : cookies) {
            if ("auth-token".equals(cookie.getName())) {
                log.debug("[JwtFilter] 쿠키에서 토큰 추출 성공");
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 헤더에서 토큰 추출
     */
    private String getTokenFromHeader(HttpServletRequest request) {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("[JwtFilter] Authorization 헤더: {}", authorization);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            log.debug("[JwtFilter] 헤더에서 토큰 추출 성공: {}", token);
            return token;
        }
        log.debug("[JwtFilter] 유효한 Authorization 헤더가 없습니다");
        return null;
    }

    /**
     * 토큰 처리 및 인증 설정
     * 
     * @return 인증 성공 여부
     */
    private boolean processToken(String token, HttpServletRequest request) {
        // 토큰 만료 검사
        if (JwtUtil.isExpired(token, secretKey)) {
            log.debug("[JwtFilter] 만료된 토큰입니다");
            return false;
        }

        // 토큰에서 이메일 추출
        String email = JwtUtil.getEmail(token, secretKey);
        if (email == null) {
            log.debug("[JwtFilter] 토큰에서 이메일을 추출할 수 없습니다");
            return false;
        }

        // 이미 인증된 상태라면 처리하지 않음
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("[JwtFilter] 이미 인증된 사용자입니다: {}", email);
            return true;
        }

        try {
            // 사용자 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 인증 객체 생성 및 설정
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("[JwtFilter] 인증 완료: {}", email);
            return true;

        } catch (UsernameNotFoundException e) {
            log.error("[JwtFilter] 사용자를 찾을 수 없습니다: {}", email);
            return false;
        } catch (Exception e) {
            log.error("[JwtFilter] 인증 처리 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
}