package com.diary.api.controller;

import com.diary.api.common.ApiResponse;
import com.diary.api.diary.Diary;
import com.diary.api.service.DiaryService;
import com.diary.api.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "일기 관련 API")
public class DiaryController {
    private final DiaryService diaryService;

    @Operation(summary = "일기 생성", description = "새로운 일기를 생성합니다.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Diary>> createDiary(
            @Valid @RequestBody Diary diary,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diaryService.createDiary(diary, user));
    }

    @Operation(summary = "일기 조회", description = "특정 일기의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Diary>> getDiary(
            @Parameter(description = "일기 ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diaryService.getDiary(id, user));
    }

    @Operation(summary = "일기 수정", description = "기존 일기를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Diary>> updateDiary(
            @Parameter(description = "일기 ID") @PathVariable Long id,
            @Valid @RequestBody Diary diary,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diaryService.updateDiary(id, diary, user));
    }

    @Operation(summary = "일기 삭제", description = "일기를 삭제합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(
            @Parameter(description = "일기 ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diaryService.deleteDiary(id, user));
    }

    @Operation(summary = "사용자 일기 목록 조회", description = "특정 사용자의 일기 목록을 조회합니다.")
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<Diary>>> getUserDiaries(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(diaryService.getUserDiaries(user, pageable));
    }

    @Operation(summary = "기간별 일기 조회", description = "특정 기간의 일기를 조회합니다.")
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<Diary>>> getDiariesByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(diaryService.getPublicDiariesByDateRange(start, end, pageable));
    }

    @Operation(summary = "감정별 일기 조회", description = "특정 감정의 일기를 조회합니다.")
    @GetMapping("/emotion/{emotion}")
    public ResponseEntity<ApiResponse<List<Diary>>> getDiariesByEmotion(
            @Parameter(description = "감정") @PathVariable String emotion) {
        return ResponseEntity.ok(diaryService.getDiariesByEmotion(emotion));
    }

    @Operation(summary = "태그별 일기 조회", description = "특정 태그의 일기를 조회합니다.")
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<Diary>>> getDiariesByTags(
            @RequestParam List<String> tags) {
        return ResponseEntity.ok(diaryService.getDiariesByTags(tags));
    }
} 