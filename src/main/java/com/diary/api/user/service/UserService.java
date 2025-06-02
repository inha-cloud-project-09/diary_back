package com.diary.api.user.service;

import com.diary.api.user.User;
import com.diary.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    /**
     * 구글 로그인 사용자 정보로 새 사용자 생성 또는 기존 사용자 정보 업데이트
     */
    @Transactional
    public User createGoogleUser(String email, String googleId, String name) {
        // 이메일로 기존 사용자 검색
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            log.info("이미 존재하는 사용자: {}", email);
            return existingUser.get();
        }

        // 새 사용자 생성
        User newUser = User.builder()
                .email(email)
                .googleId(googleId)  // User 엔티티의 googleId 필드에 sub 값 저장
                .build();

        log.info("새 구글 사용자 생성: {}", email);
        return userRepository.save(newUser);
    }

    /**
     * 이메일로 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}