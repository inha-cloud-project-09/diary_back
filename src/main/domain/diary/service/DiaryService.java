package com.diary.api.service;

import com.diary.api.common.ApiResponse;
import com.diary.api.common.exception.BusinessException;
import com.diary.api.common.exception.ResourceNotFoundException;
import com.diary.api.diary.Diary;
import com.diary.api.diary.repository.DiaryRepository;
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
public class DiaryService {
    private final DiaryRepository diaryRepository;

    @Transactional
    public ApiResponse<Diary> createDiary(Diary diary, User user) {
        try {
            diary.setUser(user);
            Diary savedDiary = diaryRepository.save(diary);
            return ApiResponse.success(savedDiary);
        } catch (Exception e) {
            log.error("일기 생성 중 오류 발생", e);
            throw new BusinessException("일기 생성에 실패했습니다.", e);
        }
    }

    public ApiResponse<Diary> getDiary(Long id, User user) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("일기를 찾을 수 없습니다."));
        
        if (!diary.isAccessibleBy(user)) {
            throw new BusinessException("해당 일기에 접근할 권한이 없습니다.");
        }
        
        return ApiResponse.success(diary);
    }

    @Transactional
    public ApiResponse<Diary> updateDiary(Long id, Diary updateDiary, User user) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("일기를 찾을 수 없습니다."));
        
        if (!diary.isOwner(user)) {
            throw new BusinessException("해당 일기를 수정할 권한이 없습니다.");
        }
        
        diary.update(updateDiary);
        return ApiResponse.success(diary);
    }

    @Transactional
    public ApiResponse<Void> deleteDiary(Long id, User user) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("일기를 찾을 수 없습니다."));
        
        if (!diary.isOwner(user)) {
            throw new BusinessException("해당 일기를 삭제할 권한이 없습니다.");
        }
        
        diaryRepository.delete(diary);
        return ApiResponse.success(null);
    }

    public ApiResponse<Page<Diary>> getUserDiaries(User user, Pageable pageable) {
        Page<Diary> diaries = diaryRepository.findByUser(user, pageable);
        return ApiResponse.success(diaries);
    }

    public ApiResponse<List<Diary>> getUserDiariesByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        List<Diary> diaries = diaryRepository.findByUserAndCreatedAtBetween(user, start, end);
        return ApiResponse.success(diaries);
    }

    public ApiResponse<List<Diary>> getPublicDiariesByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        List<Diary> diaries = diaryRepository.findByIsPublicTrueAndCreatedAtBetween(start, end, pageable);
        return ApiResponse.success(diaries);
    }

    public ApiResponse<List<Diary>> getDiariesByEmotion(String emotion) {
        List<Diary> diaries = diaryRepository.findByPrimaryEmotion(emotion);
        return ApiResponse.success(diaries);
    }

    public ApiResponse<List<Diary>> getDiariesByTags(List<String> tags) {
        List<Diary> diaries = diaryRepository.findByTagsContaining(tags);
        return ApiResponse.success(diaries);
    }
} 