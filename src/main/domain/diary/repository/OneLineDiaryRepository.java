package com.diary.api.diary;

import com.diary.api.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OneLineDiaryRepository extends JpaRepository<OneLineDiary, Long> {
    Page<OneLineDiary> findByUser(User user, Pageable pageable);
    List<OneLineDiary> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    List<OneLineDiary> findByIsPublicTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<OneLineDiary> findByAnalysisStatus(String status);
} 