package com.diary.api.domain.diary.service;

import com.diary.api.common.ApiResponse;
import com.diary.api.common.exception.BusinessException;
import com.diary.api.common.exception.ResourceNotFoundException;
import com.diary.api.domain.diary.entity.OneLineDiary;
import com.diary.api.domain.diary.repository.OneLineDiaryRepository;
import com.diary.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OneLineDiaryService {
    private final OneLineDiaryRepository oneLineDiaryRepository;

    @Transactional
    public ApiResponse<OneLineDiary> createOneLineDiary(OneLineDiary oneLineDiary, User user) {
        try {
            oneLineDiary.setUser(user);
            OneLineDiary savedOneLineDiary = oneLineDiaryRepository.save(oneLineDiary);
            return ApiResponse.success(savedOneLineDiary);
        } catch (Exception e) {
            log.error("한 줄 일기 생성 중 오류 발생", e);
            throw new BusinessException("한 줄 일기 생성에 실패했습니다.");
        }
    }

    public ApiResponse<OneLineDiary> getOneLineDiary(Long id, User user) {
        try {
            OneLineDiary oneLineDiary = oneLineDiaryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("한 줄 일기를 찾을 수 없습니다."));

            if (!oneLineDiary.getUser().equals(user) && !oneLineDiary.getIsPublic()) {
                throw new BusinessException("접근 권한이 없습니다.");
            }

            return ApiResponse.success(oneLineDiary);
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("한 줄 일기 조회 중 오류 발생", e);
            throw new BusinessException("한 줄 일기 조회에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<OneLineDiary> updateOneLineDiary(Long id, OneLineDiary updateOneLineDiary, User user) {
        try {
            OneLineDiary oneLineDiary = oneLineDiaryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("한 줄 일기를 찾을 수 없습니다."));

            if (!oneLineDiary.getUser().equals(user)) {
                throw new BusinessException("수정 권한이 없습니다.");
            }

            oneLineDiary.update(updateOneLineDiary);
            return ApiResponse.success(oneLineDiary);
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("한 줄 일기 수정 중 오류 발생", e);
            throw new BusinessException("한 줄 일기 수정에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<Void> deleteOneLineDiary(Long id, User user) {
        try {
            OneLineDiary oneLineDiary = oneLineDiaryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("한 줄 일기를 찾을 수 없습니다."));

            if (!oneLineDiary.getUser().equals(user)) {
                throw new BusinessException("삭제 권한이 없습니다.");
            }

            oneLineDiaryRepository.delete(oneLineDiary);
            return ApiResponse.success(null);
        } catch (BusinessException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("한 줄 일기 삭제 중 오류 발생", e);
            throw new BusinessException("한 줄 일기 삭제에 실패했습니다.");
        }
    }

    public ApiResponse<List<OneLineDiary>> getUserOneLineDiaries(User user) {
        try {
            return ApiResponse.success(oneLineDiaryRepository.findByUser(user));
        } catch (Exception e) {
            log.error("사용자 한 줄 일기 목록 조회 중 오류 발생", e);
            throw new BusinessException("한 줄 일기 목록 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<OneLineDiary>> getOneLineDiariesByEmotion(String emotion) {
        try {
            return ApiResponse.success(oneLineDiaryRepository.findByPrimaryEmotionAndIsPublicTrue(emotion));
        } catch (Exception e) {
            log.error("감정별 한 줄 일기 조회 중 오류 발생", e);
            throw new BusinessException("감정별 한 줄 일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<OneLineDiary>> getOneLineDiariesByTags(List<String> tags) {
        try {
            return ApiResponse.success(oneLineDiaryRepository.findByTagsInAndIsPublicTrue(tags));
        } catch (Exception e) {
            log.error("태그별 한 줄 일기 조회 중 오류 발생", e);
            throw new BusinessException("태그별 한 줄 일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<OneLineDiary>> getUserOneLineDiariesByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        try {
            return ApiResponse.success(oneLineDiaryRepository.findByUserAndCreatedAtBetween(user, start, end));
        } catch (Exception e) {
            log.error("기간별 한 줄 일기 조회 중 오류 발생", e);
            throw new BusinessException("기간별 한 줄 일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<OneLineDiary>> getOneLineDiariesByAnalysisStatus(String status) {
        try {
            return ApiResponse.success(oneLineDiaryRepository.findByAnalysisStatus(status));
        } catch (Exception e) {
            log.error("분석 상태별 한 줄 일기 조회 중 오류 발생", e);
            throw new BusinessException("분석 상태별 한 줄 일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<OneLineDiary>> getUserOneLineDiariesByAnalysisStatus(User user, String status) {
        try {
            return ApiResponse.success(oneLineDiaryRepository.findByUserAndAnalysisStatus(user, status));
        } catch (Exception e) {
            log.error("사용자의 분석 상태별 한 줄 일기 조회 중 오류 발생", e);
            throw new BusinessException("분석 상태별 한 줄 일기 조회에 실패했습니다.");
        }
    }
} 