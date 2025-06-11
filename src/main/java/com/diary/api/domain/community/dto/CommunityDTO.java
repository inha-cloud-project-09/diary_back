package com.diary.api.domain.community.dto;

import com.diary.api.domain.community.entity.Community;
import com.diary.api.domain.community.entity.CommunityMember;
import com.diary.api.domain.user.dto.UserDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CommunityDTO {
    private Long id;
    private String name;
    private String description;
    private String emotionTheme;
    private Boolean isDefault;
    private UserDTO creator;
    private LocalDateTime createdAt;
    private List<CommunityMemberDTO> members;

    public static CommunityDTO from(Community community) {
        return CommunityDTO.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .emotionTheme(community.getEmotionTheme())
                .isDefault(community.getIsDefault())
                .creator(community.getCreator() != null ? UserDTO.from(community.getCreator()) : null)
                .createdAt(community.getCreatedAt())
                .members(community.getMembers() != null ? community.getMembers().stream()
                        .map(CommunityMemberDTO::from)
                        .collect(Collectors.toList()) : null)
                .build();
    }
}