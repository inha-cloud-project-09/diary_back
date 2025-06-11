package com.diary.api.domain.community.dto;

import com.diary.api.domain.community.entity.CommunityMember;
import com.diary.api.domain.user.dto.UserDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommunityMemberDTO {
    private Long id;
    private Long userId;
    private Long communityId;
    private UserDTO user;
    private LocalDateTime joinedAt;
    private Boolean isActive;

    public static CommunityMemberDTO from(CommunityMember member) {
        return CommunityMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .communityId(member.getCommunityId())
                .user(UserDTO.from(member.getUser()))
                .joinedAt(member.getJoinedAt())
                .isActive(member.getIsActive())
                .build();
    }
}