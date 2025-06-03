package com.diary.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "diaries")
@Getter
@Setter
@NoArgsConstructor
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "json")
    private String tags;

    @Column(name = "emotion_vector", columnDefinition = "json")
    private String emotionVector;

    @Column(name = "primary_emotion")
    private String primaryEmotion;

    @Column(name = "analysis_status")
    private String analysisStatus = "pending";

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 