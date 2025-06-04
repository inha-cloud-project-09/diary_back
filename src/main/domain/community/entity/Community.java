package com.diary.api.community;

import com.diary.api.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "communities")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "emotion_theme", length = 50)
    private String emotionTheme;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityMember> members = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        isDefault = isDefault == null ? true : isDefault;
        createdAt = LocalDateTime.now();
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public void update(Community updateCommunity) {
        this.name = updateCommunity.getName();
        this.description = updateCommunity.getDescription();
        this.emotionTheme = updateCommunity.getEmotionTheme();
    }

    public void addMember(CommunityMember member) {
        this.members.add(member);
    }

    public void removeMember(CommunityMember member) {
        this.members.remove(member);
    }
}