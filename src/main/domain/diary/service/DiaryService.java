package com.diary.api.service;

import com.diary.api.common.ApiResponse;
import com.diary.api.common.exception.BusinessException;
import com.diary.api.diary.Diary;
import com.diary.api.dto.diary.CreateDiaryRequest;
import com.diary.api.dto.diary.UpdateDiaryRequest;
import com.diary.api.repository.DiaryRepository;
import com.diary.api.user.User;
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
public class DiaryService {
    private final DiaryRepository diaryRepository;

    @Transactional
    public ApiResponse<Diary> createDiary(CreateDiaryRequest request, User user) {
        try {
            Diary diary = Diary.builder()
                    .user(user)
                    .content(request.getContent())
                    .isPublic(request.getIsPublic())
                    .imageUrl(request.getImageUrl())
                    .analysisStatus("pending")
                    .build();

            Diary savedDiary = diaryRepository.save(diary);
            return ApiResponse.success(savedDiary);
        } catch (Exception e) {
            log.error("일기 생성 중 오류 발생", e);
            throw new BusinessException("일기 생성에 실패했습니다.");
        }
    }

    public ApiResponse<Diary> getDiary(Long id, User user) {
        try {
            Diary diary = diaryRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("일기를 찾을 수 없습니다."));

            if (!diary.getIsPublic() && !diary.getUser().getId().equals(user.getId())) {
                throw new BusinessException("해당 일기에 접근할 권한이 없습니다.");
            }

            return ApiResponse.success(diary);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("일기 조회 중 오류 발생", e);
            throw new BusinessException("일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<Diary>> getUserDiaries(User user) {
        try {
            List<Diary> diaries = diaryRepository.findByUser(user);
            return ApiResponse.success(diaries);
        } catch (Exception e) {
            log.error("사용자 일기 조회 중 오류 발생", e);
            throw new BusinessException("일기 조회에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<Diary> updateDiary(Long id, UpdateDiaryRequest request, User user) {
        try {
            Diary diary = diaryRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("일기를 찾을 수 없습니다."));

            if (!diary.getUser().getId().equals(user.getId())) {
                throw new BusinessException("일기 수정 권한이 없습니다.");
            }

            diary.updateContent(request.getContent());
            diary.setIsPublic(request.getIsPublic());
            diary.setImageUrl(request.getImageUrl());
            diary.setEmotion(request.getEmotion());
            diary.setTags(request.getTags());

            return ApiResponse.success(diary);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("일기 수정 중 오류 발생", e);
            throw new BusinessException("일기 수정에 실패했습니다.");
        }
    }

    @Transactional
    public ApiResponse<Void> deleteDiary(Long id, User user) {
        try {
            Diary diary = diaryRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("일기를 찾을 수 없습니다."));

            if (!diary.getUser().getId().equals(user.getId())) {
                throw new BusinessException("일기 삭제 권한이 없습니다.");
            }

            diaryRepository.delete(diary);
            return ApiResponse.success(null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("일기 삭제 중 오류 발생", e);
            throw new BusinessException("일기 삭제에 실패했습니다.");
        }
    }

    public ApiResponse<List<Diary>> getDiariesByEmotion(String emotion) {
        try {
            List<Diary> diaries = diaryRepository.findByPrimaryEmotionAndIsPublicTrue(emotion);
            return ApiResponse.success(diaries);
        } catch (Exception e) {
            log.error("감정별 일기 조회 중 오류 발생", e);
            throw new BusinessException("감정별 일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<Diary>> getDiariesByTags(List<String> tags) {
        try {
            List<Diary> diaries = diaryRepository.findByTagsInAndIsPublicTrue(tags);
            return ApiResponse.success(diaries);
        } catch (Exception e) {
            log.error("태그별 일기 조회 중 오류 발생", e);
            throw new BusinessException("태그별 일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<Diary>> getUserDiariesByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        try {
            List<Diary> diaries = diaryRepository.findUserDiariesByDateRange(user, start, end);
            return ApiResponse.success(diaries);
        } catch (Exception e) {
            log.error("기간별 일기 조회 중 오류 발생", e);
            throw new BusinessException("기간별 일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<Diary>> getDiariesByAnalysisStatus(String status) {
        try {
            List<Diary> diaries = diaryRepository.findByAnalysisStatus(status);
            return ApiResponse.success(diaries);
        } catch (Exception e) {
            log.error("분석 상태별 일기 조회 중 오류 발생", e);
            throw new BusinessException("분석 상태별 일기 조회에 실패했습니다.");
        }
    }

    public ApiResponse<List<Diary>> getUserDiariesByAnalysisStatus(User user, String status) {
        try {
            List<Diary> diaries = diaryRepository.findByUserAndAnalysisStatus(user, status);
            return ApiResponse.success(diaries);
        } catch (Exception e) {
            log.error("사용자의 분석 상태별 일기 조회 중 오류 발생", e);
            throw new BusinessException("분석 상태별 일기 조회에 실패했습니다.");
        }
    }
} 