package com.diary.api.domain.community.service;

import com.diary.api.common.ApiResponse;
import com.diary.api.common.exception.BusinessException;
import com.diary.api.common.exception.ResourceNotFoundException;
import com.diary.api.domain.community.entity.Community;
import com.diary.api.domain.community.entity.CommunityMember;
import com.diary.api.domain.diary.entity.Diary;
import com.diary.api.domain.diary.repository.DiaryRepository;
import com.diary.api.domain.user.entity.User;
import com.diary.api.domain.community.repository.CommunityRepository;
import com.diary.api.domain.community.repository.CommunityMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public ApiResponse<Community> createCommunity(Community community, User user) {
        try {
            community.setCreator(user);
            Community savedCommunity = communityRepository.save(community);

            // 생성자를 자동으로 멤버로 추가
            CommunityMember member = CommunityMember.create(user, savedCommunity);
            communityMemberRepository.save(member);

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

            // creator와 members 관계를 명시적으로 로드
            community.getCreator();
            community.getMembers().size();

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
                return ApiResponse
                        .success(communityRepository.findByEmotionThemeAndNameContaining(emotionTheme, keyword));
            }
            return ApiResponse.success(communityRepository.findByNameContaining(keyword));
        } catch (Exception e) {
            log.error("커뮤니티 검색 중 오류 발생", e);
            throw new BusinessException("커뮤니티 검색에 실패했습니다.");
        }
    }

    public ApiResponse<List<Community>> getUserCommunities(User user) {
        try {
            return ApiResponse.success(communityRepository.findByUserIdAndIsActive(user.getId()));
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

            // members 컬렉션을 명시적으로 로드
            community.getMembers().size();

            // 이미 가입되어 있는지 확인
            boolean isAlreadyMember = community.getMembers().stream()
                    .anyMatch(member -> member.getUserId().equals(user.getId()) &&
                            Boolean.TRUE.equals(member.getIsActive()));

            if (isAlreadyMember) {
                throw new BusinessException("이미 가입된 커뮤니티입니다.");
            }

            // 새로운 멤버십 생성
            CommunityMember member = CommunityMember.create(user, community);
            communityMemberRepository.save(member);

            return ApiResponse.success(null);
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("커뮤니티 가입 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException("커뮤니티 가입에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<Void> leaveCommunity(Long id, User user) {
        try {
            Community community = communityRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("커뮤니티를 찾을 수 없습니다."));

            // 활성 멤버십 찾기
            CommunityMember member = communityRepository.findActiveMembership(user.getId(), community.getId())
                    .orElseThrow(() -> new BusinessException("가입하지 않은 커뮤니티입니다."));

            // 멤버십 비활성화
            member.setIsActive(false);
            communityMemberRepository.save(member);

            return ApiResponse.success(null);
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("커뮤니티 탈퇴 중 오류 발생", e);
            throw new BusinessException("커뮤니티 탈퇴에 실패했습니다.");
        }
    }

    /**
     * 커뮤니티 멤버들이 작성한 공개 일기 중 최신 10개를 조회합니다.
     * 
     * @param communityId 커뮤니티 ID
     * @return 공개된 일기 목록 (최신순 상위 10개)
     */
    public ApiResponse<List<Map<String, Object>>> getPublicCommunityDiaries(Long communityId) {
        try {
            Community community = communityRepository.findById(communityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Community not found with id: " + communityId));

            // 활성 멤버들의 ID 목록 추출
            List<Long> memberIds = community.getMembers().stream()
                    .filter(CommunityMember::getIsActive)
                    .map(member -> member.getUser().getId())
                    .collect(Collectors.toList());

            // 최신순으로 상위 10개만 조회
            PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<Map<String, Object>> diaries = diaryRepository.findTop10ByUserIdInOrderByCreatedAtDesc(memberIds,
                    pageRequest);

            return ApiResponse.success(diaries);
        } catch (ResourceNotFoundException e) {
            log.error("Community not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching community diaries: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch community diaries", e);
        }
    }
}