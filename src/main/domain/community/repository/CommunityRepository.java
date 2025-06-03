package com.diary.repository;

import com.diary.domain.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    Page<Community> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    List<Community> findByAuthor(String author);
    List<Community> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Community> findAllByOrderByViewCountDesc(Pageable pageable);
    List<Community> findAllByOrderByLikeCountDesc(Pageable pageable);
} 