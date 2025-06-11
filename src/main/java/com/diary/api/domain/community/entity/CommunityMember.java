package com.diary.api.domain.community.entity;

import com.diary.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_members")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", insertable = false, updatable = false)
    private Community community;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        isActive = isActive == null ? true : isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public static CommunityMember create(User user, Community community) {
        return CommunityMember.builder()
                .userId(user.getId())
                .communityId(community.getId())
                .user(user)
                .community(community)
                .isActive(true)
                .build();
    }
}