package com.diary.repository;

import com.diary.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Diary> findByTitleContaining(String keyword);
} 