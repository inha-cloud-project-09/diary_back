package com.diary.api.diary;

import com.diary.api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OneLineDiaryService {
    private final OneLineDiaryRepository oneLineDiaryRepository;

    @Transactional
    public OneLineDiary createOneLineDiary(OneLineDiary oneLineDiary) {
        return oneLineDiaryRepository.save(oneLineDiary);
    }

    public OneLineDiary getOneLineDiary(Long id) {
        return oneLineDiaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("One line diary not found"));
    }

    public Page<OneLineDiary> getUserOneLineDiaries(User user, Pageable pageable) {
        return oneLineDiaryRepository.findByUser(user, pageable);
    }

    public List<OneLineDiary> getUserOneLineDiariesByDateRange(User user, LocalDateTime start, LocalDateTime end) {
        return oneLineDiaryRepository.findByUserAndCreatedAtBetween(user, start, end);
    }

    public List<OneLineDiary> getPublicOneLineDiariesByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return oneLineDiaryRepository.findByIsPublicTrueAndCreatedAtBetween(start, end, pageable);
    }

    public List<OneLineDiary> getOneLineDiariesByAnalysisStatus(String status) {
        return oneLineDiaryRepository.findByAnalysisStatus(status);
    }

    @Transactional
    public OneLineDiary updateOneLineDiary(Long id, OneLineDiary updatedDiary) {
        OneLineDiary diary = getOneLineDiary(id);
        diary.setContent(updatedDiary.getContent());
        diary.setIsPublic(updatedDiary.getIsPublic());
        diary.setTags(updatedDiary.getTags());
        return oneLineDiaryRepository.save(diary);
    }

    @Transactional
    public OneLineDiary updateAnalysisResult(Long id, String primaryEmotion, Map<String, Double> emotionVector, String status) {
        OneLineDiary diary = getOneLineDiary(id);
        diary.setPrimaryEmotion(primaryEmotion);
        diary.setEmotionVector(emotionVector);
        diary.setAnalysisStatus(status);
        return oneLineDiaryRepository.save(diary);
    }

    @Transactional
    public void deleteOneLineDiary(Long id) {
        oneLineDiaryRepository.deleteById(id);
    }
} 