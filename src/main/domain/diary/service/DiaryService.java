package com.diary.service;

import com.diary.domain.Diary;
import com.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {
    private final DiaryRepository diaryRepository;

    @Transactional
    public Diary createDiary(Diary diary) {
        return diaryRepository.save(diary);
    }

    public Diary getDiary(Long id) {
        return diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diary not found"));
    }

    public List<Diary> getAllDiaries() {
        return diaryRepository.findAll();
    }

    public List<Diary> getDiariesByDateRange(LocalDateTime start, LocalDateTime end) {
        return diaryRepository.findByCreatedAtBetween(start, end);
    }

    public List<Diary> searchDiariesByTitle(String keyword) {
        return diaryRepository.findByTitleContaining(keyword);
    }

    @Transactional
    public Diary updateDiary(Long id, Diary updatedDiary) {
        Diary diary = getDiary(id);
        diary.setTitle(updatedDiary.getTitle());
        diary.setContent(updatedDiary.getContent());
        return diaryRepository.save(diary);
    }

    @Transactional
    public void deleteDiary(Long id) {
        diaryRepository.deleteById(id);
    }
} 