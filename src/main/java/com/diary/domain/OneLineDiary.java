package com.diary.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "one_line_diaries")
@Getter
@Setter
@NoArgsConstructor
public class OneLineDiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(columnDefinition = "json")
    private String tags;

    @Column(name = "emotion_vector", columnDefinition = "json")
    private String emotionVector;

    @Column(name = "primary_emotion")
    private String primaryEmotion;

    @Column(name = "analysis_status")
    private String analysisStatus = "pending";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 