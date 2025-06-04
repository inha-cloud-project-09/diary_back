package com.diary.api.community.service;

import com.diary.api.common.ApiResponse;
import com.diary.api.common.exception.BusinessException;
import com.diary.api.common.exception.ResourceNotFoundException;
import com.diary.api.community.Community;
import com.diary.api.community.CommunityMember;
import com.diary.api.community.repository.CommunityRepository;
import com.diary.api.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        try {
            Community community = communityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));
            return ApiResponse.success(community);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("커뮤니티 조회 중 오류 발생", e);
            throw new BusinessException("커뮤니티 조회에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<Community> updateCommunity(Long id, Community updateCommunity, User user) {
        try {
            Community community = communityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

            if (!community.getCreator().equals(user)) {
                throw new BusinessException("커뮤니티 수정 권한이 없습니다.");
            }

            community.update(updateCommunity);
            return ApiResponse.success(community);
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("커뮤니티 수정 중 오류 발생", e);
            throw new BusinessException("커뮤니티 수정에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<Void> deleteCommunity(Long id, User user) {
        try {
            Community community = communityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

            if (!community.getCreator().equals(user)) {
                throw new BusinessException("커뮤니티 삭제 권한이 없습니다.");
            }

            communityRepository.delete(community);
            return ApiResponse.success(null);
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("커뮤니티 삭제 중 오류 발생", e);
            throw new BusinessException("커뮤니티 삭제에 실패했습니다.");
        }
    }

    public ApiResponse<List<Community>> getDefaultCommunities() {
        try {
            return ApiResponse.success(communityRepository.findByIsDefaultTrue());
        } catch (Exception e) {
            log.error("기본 커뮤니티 조회 중 오류 발생", e);
            throw new BusinessException("기본 커뮤니티 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<Community>> getCommunitiesByEmotionTheme(String emotionTheme) {
        try {
            return ApiResponse.success(communityRepository.findByEmotionTheme(emotionTheme));
        } catch (Exception e) {
            log.error("감정 테마별 커뮤니티 조회 중 오류 발생", e);
            throw new BusinessException("감정 테마별 커뮤니티 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<Community>> searchCommunities(String keyword, String emotionTheme) {
        try {
            if (emotionTheme != null && !emotionTheme.isEmpty()) {
                return ApiResponse.success(communityRepository.findByEmotionThemeAndNameContaining(emotionTheme, keyword));
            }
            return ApiResponse.success(communityRepository.findByNameContaining(keyword));
        } catch (Exception e) {
            log.error("커뮤니티 검색 중 오류 발생", e);
            throw new BusinessException("커뮤니티 검색에 실패했습니다.");
        }
    }

    public ApiResponse<List<Community>> getUserCommunities(User user) {
        try {
            return ApiResponse.success(communityRepository.findByUserAndIsActive(user));
        } catch (Exception e) {
            log.error("사용자의 커뮤니티 조회 중 오류 발생", e);
            throw new BusinessException("사용자의 커뮤니티 조회에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<Void> joinCommunity(Long id, User user) {
        try {
            Community community = communityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

            if (community.getMembers().stream().anyMatch(m -> m.getUser().equals(user) && m.getIsActive())) {
                throw new BusinessException("이미 가입된 커뮤니티입니다.");
            }

            CommunityMember member = CommunityMember.builder()
                    .user(user)
                    .community(community)
                    .isActive(true)
                    .build();
            community.getMembers().add(member);
            return ApiResponse.success(null);
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("커뮤니티 가입 중 오류 발생", e);
            throw new BusinessException("커뮤니티 가입에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<Void> leaveCommunity(Long id, User user) {
        try {
            Community community = communityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

            community.getMembers().stream()
                    .filter(m -> m.getUser().equals(user) && m.getIsActive())
                    .findFirst()
                    .ifPresent(m -> m.setIsActive(false));

            return ApiResponse.success(null);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("커뮤니티 탈퇴 중 오류 발생", e);
            throw new BusinessException("커뮤니티 탈퇴에 실패했습니다.");
        }
    }
} 