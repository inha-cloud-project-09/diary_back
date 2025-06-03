package com.diary.api.diary.controller;

import com.diary.api.diary.Diary;
import com.diary.api.diary.service.DiaryService;
import com.diary.api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDiaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String mood,
            @RequestParam(required = false) List<String> tags) {
        
        Sort sortOption = switch (sort) {
            case "popular" -> Sort.by("viewCount").descending();
            case "oldest" -> Sort.by("createdAt").ascending();
            default -> Sort.by("createdAt").descending();
        };

        PageRequest pageRequest = PageRequest.of(page, limit, sortOption);
        return ResponseEntity.ok(diaryService.getDiariesWithFilters(pageRequest, mood, tags));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDiary(@PathVariable Long id) {
        return ResponseEntity.ok(diaryService.getDiaryWithDetails(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDiary(
            @AuthenticationPrincipal User user,
            @RequestBody Diary diary) {
        diary.setUser(user);
        return ResponseEntity.ok(diaryService.createDiary(diary));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDiary(
            @PathVariable Long id,
            @RequestBody Diary updatedDiary) {
        return ResponseEntity.ok(diaryService.updateDiary(id, updatedDiary));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteDiary(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/dashboard/emotions")
    public ResponseEntity<Map<String, Object>> getEmotionStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        return ResponseEntity.ok(diaryService.getEmotionStats(startDate, endDate));
    }

    @GetMapping("/dashboard/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations() {
        return ResponseEntity.ok(diaryService.getRecommendedDiaries());
    }

    @GetMapping("/cluster/my-latest-diaries")
    public ResponseEntity<Map<String, Object>> getMyLatestClusterDiaries(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(diaryService.getLatestClusterDiaries(user, limit));
    }
} 