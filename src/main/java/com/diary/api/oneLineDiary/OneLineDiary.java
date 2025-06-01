package com.diary.api.onelinediary;

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
@Table(name = "one_line_diaries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OneLineDiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_public")
    private Boolean isPublic;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emotion_vector")
    private Map<String, Double> emotionVector;

    @Column(name = "primary_emotion")
    private String primaryEmotion;

    @Column(name = "analysis_status")
    private String analysisStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        isPublic = isPublic == null ? false : isPublic;
        analysisStatus = analysisStatus == null ? "pending" : analysisStatus;
        createdAt = LocalDateTime.now();
    }
}