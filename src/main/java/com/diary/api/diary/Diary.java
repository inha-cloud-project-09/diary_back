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
@Table(name = "diary")
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

    @Column(name = "primary_emotion")
    private String primaryEmotion;

    @Column(name = "analysis_status")
    private String analysisStatus;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        isPublic = isPublic == null ? false : isPublic;
        analysisStatus = analysisStatus == null ? "pending" : analysisStatus;
        createdAt = LocalDateTime.now();
    }
}