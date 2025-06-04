package com.diary.api.domain.diary.repository;

import com.diary.api.domain.diary.entity.Diary;
import com.diary.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    // 사용자의 모든 일기 조회
    List<Diary> findByUser(User user);
    
    // 공개된 일기 중 감정별 조회
    List<Diary> findByPrimaryEmotionAndIsPublicTrue(String emotion);
    
    // 공개된 일기 중 태그로 조회
    @Query(value = "SELECT * FROM diary d WHERE d.is_public = true AND JSON_CONTAINS(d.tags, :tag)", nativeQuery = true)
    List<Diary> findByTagsContainingAndIsPublicTrue(@Param("tag") String tag);
    
    // 공개된 일기 중 여러 태그로 조회
    @Query(value = "SELECT * FROM diary d WHERE d.is_public = true AND d.tags IN :tags", nativeQuery = true)
    List<Diary> findByTagsInAndIsPublicTrue(@Param("tags") List<String> tags);
    
    // 사용자의 일기 중 특정 기간 조회
    @Query("SELECT d FROM Diary d WHERE d.user = :user AND d.createdAt BETWEEN :start AND :end ORDER BY d.createdAt DESC")
    List<Diary> findUserDiariesByDateRange(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    
    // 분석 대기 중인 일기 조회
    @Query("SELECT d FROM Diary d WHERE d.analysisStatus = 'pending' ORDER BY d.createdAt ASC")
    List<Diary> findPendingAnalysisDiaries();
    
    // 분석 상태별 일기 조회
    List<Diary> findByAnalysisStatus(String status);
    
    // 사용자의 분석 상태별 일기 조회
    List<Diary> findByUserAndAnalysisStatus(User user, String status);
} 