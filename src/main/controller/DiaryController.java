package com.diary.controller;

import com.diary.domain.Diary;
import com.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<Diary> createDiary(@RequestBody Diary diary) {
        return ResponseEntity.ok(diaryService.createDiary(diary));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Diary> getDiary(@PathVariable Long id) {
        return ResponseEntity.ok(diaryService.getDiary(id));
    }

    @GetMapping
    public ResponseEntity<List<Diary>> getAllDiaries() {
        return ResponseEntity.ok(diaryService.getAllDiaries());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Diary>> getDiariesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(diaryService.getDiariesByDateRange(start, end));
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Diary>> searchDiariesByTitle(@RequestParam String keyword) {
        return ResponseEntity.ok(diaryService.searchDiariesByTitle(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Diary> updateDiary(@PathVariable Long id, @RequestBody Diary updatedDiary) {
        return ResponseEntity.ok(diaryService.updateDiary(id, updatedDiary));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        return ResponseEntity.ok().build();
    }
} 