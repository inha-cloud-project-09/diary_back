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
    List<Community> findAllByUserId(String userId);
    List<Community> findByUserIdAndCommunityId(String userId, Long communityId);
} 