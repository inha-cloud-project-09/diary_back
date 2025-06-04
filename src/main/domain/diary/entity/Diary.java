package com.diary.api.diary;

import com.diary.api.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "diaries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(columnDefinition = "text")
    private String feedback;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emotion_vector")
    private Map<String, Double> emotionVector;

    @Column(name = "primary_emotion", length = 50)
    private String primaryEmotion;

    @Column(name = "analysis_status", length = 20)
    private String analysisStatus;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        isPublic = isPublic == null ? false : isPublic;
        analysisStatus = analysisStatus == null ? "pending" : analysisStatus;
        createdAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setEmotion(String emotion) {
        this.primaryEmotion = emotion;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public void updateAnalysisStatus(String status) {
        this.analysisStatus = status;
        if ("completed".equals(status)) {
            this.analyzedAt = LocalDateTime.now();
        }
    }
}