package com.diary.api.domain.community.repository;

import com.diary.api.domain.community.entity.CommunityMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {
}