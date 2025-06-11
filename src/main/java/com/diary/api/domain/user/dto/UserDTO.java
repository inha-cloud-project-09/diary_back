package com.diary.api.domain.user.dto;

import com.diary.api.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String googleId;
    private LocalDateTime createdAt;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .googleId(user.getGoogleId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}