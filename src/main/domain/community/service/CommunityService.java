package com.diary.api.service;

import com.diary.api.common.ApiResponse;
import com.diary.api.common.exception.BusinessException;
import com.diary.api.common.exception.ResourceNotFoundException;
import com.diary.api.community.Community;
import com.diary.api.community.repository.CommunityRepository;
import com.diary.api.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityRepository communityRepository;

    @Transactional
    public ApiResponse<Community> createCommunity(Community community, User user) {
        try {
            community.setCreator(user);
            Community savedCommunity = communityRepository.save(community);
            return ApiResponse.success(savedCommunity);
        } catch (Exception e) {
            log.error("커뮤니티 생성 중 오류 발생", e);
            throw new BusinessException("커뮤니티 생성에 실패했습니다.");
        }
    }

    public ApiResponse<Community> getCommunity(Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));
        return ApiResponse.success(community);
    }

    @Transactional
    public ApiResponse<Community> updateCommunity(Long id, Community updateCommunity, User user) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

        if (!community.getCreator().equals(user)) {
            throw new BusinessException("커뮤니티 수정 권한이 없습니다.");
        }

        community.update(updateCommunity);
        return ApiResponse.success(community);
    }

    @Transactional
    public ApiResponse<Void> deleteCommunity(Long id, User user) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

        if (!community.getCreator().equals(user)) {
            throw new BusinessException("커뮤니티 삭제 권한이 없습니다.");
        }

        communityRepository.delete(community);
        return ApiResponse.success(null);
    }

    public ApiResponse<Page<Community>> getCommunities(Pageable pageable) {
        return ApiResponse.success(communityRepository.findAll(pageable));
    }

    public ApiResponse<List<Community>> searchCommunities(String name, String emotionTheme, String creator) {
        return ApiResponse.success(communityRepository.searchCommunities(name, emotionTheme, creator));
    }

    @Transactional
    public ApiResponse<Void> joinCommunity(Long id, User user) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

        if (community.getMembers().contains(user)) {
            throw new BusinessException("이미 가입된 커뮤니티입니다.");
        }

        community.addMember(user);
        return ApiResponse.success(null);
    }

    @Transactional
    public ApiResponse<Void> leaveCommunity(Long id, User user) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

        if (!community.getMembers().contains(user)) {
            throw new BusinessException("가입하지 않은 커뮤니티입니다.");
        }

        community.removeMember(user);
        return ApiResponse.success(null);
    }
} 