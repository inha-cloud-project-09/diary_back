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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 접근 권한
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/health",
                                // Swagger UI와 API Docs 접근 허용
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/api/auth/google")
                        .permitAll()
                        .anyRequest().authenticated())

                // OAuth2 (구글 등)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(new LoginSuccessHandler(objectMapper, jwtUtil, userService, userRepository, environment)))

                // 로그아웃
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll())

                // JWT 필터 등록
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)

                // 세션 관리
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리
                .exceptionHandling(exception -> {
                    exception.accessDeniedHandler(new Http403Handler(objectMapper));
                    exception.authenticationEntryPoint(new Http401Handler(objectMapper));
                })

                // RememberMe 예시
                .rememberMe(rm -> rm
                        .rememberMeParameter("remember")
                        .alwaysRemember(false)
                        .tokenValiditySeconds(2592000))

                // CSRF 해제 (API 서버용)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public JwtFilter jwtFilter() {
        // 쿠키에서 token 가져와 검증 -> userDetailsService 로드
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
        // SCrypt 예시
        return new SCryptPasswordEncoder(16, 8, 1, 32, 64);
    }
}
