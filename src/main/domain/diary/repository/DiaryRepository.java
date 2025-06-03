package com.diary.api.diary;

import com.diary.api.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Page<Diary> findByUser(User user, Pageable pageable);
    List<Diary> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Diary> findByIsPublicTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Diary> findByAnalysisStatus(String status);
    List<Diary> findByPrimaryEmotion(String emotion);
} 