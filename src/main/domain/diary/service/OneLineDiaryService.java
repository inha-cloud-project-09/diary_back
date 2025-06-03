package com.diary.api.service;

import com.diary.api.common.ApiResponse;
import com.diary.api.common.exception.BusinessException;
import com.diary.api.common.exception.ResourceNotFoundException;
import com.diary.api.diary.OneLineDiary;
import com.diary.api.diary.repository.OneLineDiaryRepository;
import com.diary.api.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        OneLineDiary oneLineDiary = oneLineDiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("한 줄 일기를 찾을 수 없습니다."));

        if (!oneLineDiary.getUser().equals(user) && !oneLineDiary.isPublic()) {
            throw new BusinessException("접근 권한이 없습니다.");
        }

        return ApiResponse.success(oneLineDiary);
    }

    @Transactional
    public ApiResponse<OneLineDiary> updateOneLineDiary(Long id, OneLineDiary updateOneLineDiary, User user) {
        OneLineDiary oneLineDiary = oneLineDiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("한 줄 일기를 찾을 수 없습니다."));

        if (!oneLineDiary.getUser().equals(user)) {
            throw new BusinessException("수정 권한이 없습니다.");
        }

        oneLineDiary.update(updateOneLineDiary);
        return ApiResponse.success(oneLineDiary);
    }

    @Transactional
    public ApiResponse<Void> deleteOneLineDiary(Long id, User user) {
        OneLineDiary oneLineDiary = oneLineDiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("한 줄 일기를 찾을 수 없습니다."));

        if (!oneLineDiary.getUser().equals(user)) {
            throw new BusinessException("삭제 권한이 없습니다.");
        }

        oneLineDiaryRepository.delete(oneLineDiary);
        return ApiResponse.success(null);
    }

    public ApiResponse<Page<OneLineDiary>> getUserOneLineDiaries(User user, Pageable pageable) {
        return ApiResponse.success(oneLineDiaryRepository.findByUser(user, pageable));
    }

    public ApiResponse<List<OneLineDiary>> getPublicOneLineDiariesByDateRange(
            LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return ApiResponse.success(oneLineDiaryRepository.findByIsPublicTrueAndCreatedAtBetween(start, end, pageable));
    }

    public ApiResponse<List<OneLineDiary>> getOneLineDiariesByEmotion(String emotion) {
        return ApiResponse.success(oneLineDiaryRepository.findByPrimaryEmotion(emotion));
    }

    public ApiResponse<List<OneLineDiary>> getOneLineDiariesByTags(List<String> tags) {
        return ApiResponse.success(oneLineDiaryRepository.findByTagsContaining(tags));
    }
} 