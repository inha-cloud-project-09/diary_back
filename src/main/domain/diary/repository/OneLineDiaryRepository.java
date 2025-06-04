package com.diary.api.diary;

import com.diary.api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OneLineDiaryRepository extends JpaRepository<OneLineDiary, Long> {
    // 사용자의 모든 일기 조회
    List<OneLineDiary> findByUser(User user);
    
    // 사용자의 일기 중 특정 기간 조회
    @Query("SELECT d FROM OneLineDiary d WHERE d.user = :user AND d.createdAt BETWEEN :start AND :end ORDER BY d.createdAt DESC")
    List<OneLineDiary> findByUserAndCreatedAtBetween(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    
    // 공개된 일기 중 특정 기간 조회
    @Query("SELECT d FROM OneLineDiary d WHERE d.isPublic = true AND d.createdAt BETWEEN :start AND :end ORDER BY d.createdAt DESC")
    List<OneLineDiary> findByIsPublicTrueAndCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    
    // 분석 상태별 조회
    List<OneLineDiary> findByAnalysisStatus(String status);
    
    // 감정별 공개 일기 조회
    List<OneLineDiary> findByPrimaryEmotionAndIsPublicTrue(String emotion);
    
    // 태그별 공개 일기 조회
    @Query("SELECT d FROM OneLineDiary d WHERE d.isPublic = true AND :tag MEMBER OF d.tags")
    List<OneLineDiary> findByTagsContainingAndIsPublicTrue(@Param("tag") String tag);
    
    // 여러 태그로 공개 일기 조회
    @Query("SELECT d FROM OneLineDiary d WHERE d.isPublic = true AND d.tags IN :tags")
    List<OneLineDiary> findByTagsInAndIsPublicTrue(@Param("tags") List<String> tags);
    
    // 사용자의 분석 상태별 조회
    List<OneLineDiary> findByUserAndAnalysisStatus(User user, String status);
    
    // 분석 대기 중인 일기 조회
    @Query("SELECT d FROM OneLineDiary d WHERE d.analysisStatus = 'pending' ORDER BY d.createdAt ASC")
    List<OneLineDiary> findPendingAnalysisDiaries();
} 