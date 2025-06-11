package com.diary.api.domain.user.config;

import com.diary.api.domain.user.config.filter.JwtFilter;
import com.diary.api.domain.user.config.handler.Http401Handler;
import com.diary.api.domain.user.config.handler.Http403Handler;
import com.diary.api.domain.user.config.handler.LoginSuccessHandler;
import com.diary.api.domain.user.entity.User;
import com.diary.api.domain.user.repository.UserRepository;
import com.diary.api.domain.user.service.UserService;
import com.diary.api.domain.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final Environment environment;

    @Value("${jwt.secret}")
    private String secretKey;

    // LoginSuccessHandler를 빈으로 등록해 필드 주입하여 재사용
    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(objectMapper, jwtUtil, userService, userRepository, environment);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/health",
                                "/favicon.ico",
                                "/robots.txt",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/api/auth/google",
                                "/login/oauth2/code/**",
                                "/oauth2/authorization/**",
                                "/error")
                        .permitAll()
                        .anyRequest().authenticated())

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> {
                    oauth2
                            .authorizationEndpoint(authorization -> authorization
                                    .baseUri("/oauth2/authorization")
                                    .authorizationRequestRepository(authorizationRequestRepository()))
                            .redirectionEndpoint(redirection -> redirection
                                    .baseUri("/login/oauth2/code/*"))
                            .successHandler(loginSuccessHandler())
                            .failureHandler((request, response, exception) -> {
                                log.error("OAuth2 로그인 실패: {}", exception.getMessage());
                                String errorMessage = exception.getMessage();
                                if (errorMessage.contains("authorization_request_not_found")) {
                                    errorMessage = "인증 요청이 만료되었습니다. 다시 시도해주세요.";
                                }
                                response.sendRedirect("/api/auth/google?error=" + errorMessage);
                            });
                })

                // 세션을 사용하지 않고 JWT로 stateless 처리
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 로그아웃 처리
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .deleteCookies("auth-token")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll())

                // JWT 필터를 UsernamePasswordAuthenticationFilter 전에 등록
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)

                // 예외 처리 핸들러 설정
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(new Http403Handler(objectMapper))
                        .authenticationEntryPoint(new Http401Handler(objectMapper)))

                // CSRF 비활성화 (API 서버이므로)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(secretKey, userDetailsService());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException(username + " 을(를) 찾을 수 없습니다."));
            return new UserPrincipal(user);
        };
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new SCryptPasswordEncoder(16, 8, 1, 32, 64);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        String activeProfile = environment.getProperty("spring.profiles.active", "local");
        if ("local".equals(activeProfile)) {
            configuration.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://localhost:8080"));
        } else {
            String allowedOrigins = environment.getProperty("cors.allowed-origins", "https://withudiary.my");
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
}
