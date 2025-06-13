package com.diary.api.domain.chat.repository;

import com.diary.api.domain.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    Optional<ChatSession> findByIdAndUserId(String id, Long userId);
    List<ChatSession> findByUserId(Long userId);
    List<ChatSession> findByUpdatedAtBefore(LocalDateTime time);
}