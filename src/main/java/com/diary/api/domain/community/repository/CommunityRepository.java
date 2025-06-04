package com.diary.api.domain.community.repository;

import com.diary.api.domain.community.entity.Community;
import com.diary.api.domain.community.entity.CommunityMember;
import com.diary.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    // 사용자가 생성한 커뮤니티 조회
    List<Community> findByCreator(User creator);
    
    // 기본 커뮤니티 조회
    List<Community> findByIsDefaultTrue();
    
    // 감정 테마별 커뮤니티 조회
    List<Community> findByEmotionTheme(String emotionTheme);
    
    // 이름으로 커뮤니티 검색
    @Query("SELECT c FROM Community c WHERE c.name LIKE %:keyword%")
    List<Community> findByNameContaining(@Param("keyword") String keyword);
    
    // 감정 테마와 이름으로 커뮤니티 검색
    @Query("SELECT c FROM Community c WHERE c.emotionTheme = :emotionTheme AND c.name LIKE %:keyword%")
    List<Community> findByEmotionThemeAndNameContaining(
            @Param("emotionTheme") String emotionTheme,
            @Param("keyword") String keyword
    );
    
    // 사용자가 참여한 커뮤니티 조회
    @Query("SELECT c FROM Community c JOIN c.members m WHERE m.user = :user AND m.isActive = true")
    List<Community> findByUserAndIsActive(@Param("user") User user);
    
    // 사용자의 커뮤니티 멤버십 조회
    @Query("SELECT m FROM CommunityMember m WHERE m.user = :user AND m.community = :community AND m.isActive = true")
    Optional<CommunityMember> findActiveMembership(@Param("user") User user, @Param("community") Community community);
} 