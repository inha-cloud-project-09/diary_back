package com.diary.api.domain.diary.entity;

import com.diary.api.domain.diary.entity.converter.EmotionVectorConverter;
import com.diary.api.domain.diary.entity.converter.TagsConverter;
import com.diary.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
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
    @Column(columnDefinition = "jsonb")
    @Convert(converter = TagsConverter.class)
    private List<String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emotion_vector", columnDefinition = "jsonb")
    @Convert(converter = EmotionVectorConverter.class)
    private List<Double> emotionVector;

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

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        isPublic = isPublic == null ? false : isPublic;
        analysisStatus = analysisStatus == null ? "pending" : analysisStatus;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
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

    public String getEmotion() {
        return this.primaryEmotion;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void updateAnalysisStatus(String status) {
        this.analysisStatus = status;
        if ("completed".equals(status)) {
            this.analyzedAt = LocalDateTime.now();
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public void setEmotionVector(List<Double> emotionVector) {
        this.emotionVector = emotionVector;
    }
}