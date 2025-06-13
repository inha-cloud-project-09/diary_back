package com.diary.api.controller;

import com.diary.api.common.ApiResponse;
import com.diary.api.domain.diary.dto.DiaryDto;
import com.diary.api.domain.diary.entity.Diary;
import com.diary.api.domain.diary.service.DiaryService;
import com.diary.api.domain.user.config.UserPrincipal;
import com.diary.api.domain.user.entity.User;
import com.diary.api.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.*;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "일기 관련 API")
public class DiaryController {
        private final DiaryService diaryService;
        private final UserRepository userRepository;

        @Operation(summary = "일기 생성", description = "새로운 일기를 생성합니다.")
        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<Diary>> createDiary(
                        @Valid @RequestBody Diary diary,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                User user = userRepository.findByEmail(userPrincipal.getUsername())
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                return ResponseEntity.ok(diaryService.createDiary(diary, user));
        }

        @Operation(summary = "일기 조회", description = "ID로 일기를 조회합니다.")
        @GetMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<DiaryDto>> getDiary(
                @Parameter(description = "일기 ID") @PathVariable Long id,
                @AuthenticationPrincipal UserPrincipal userPrincipal) {
                User user = userRepository.findByEmail(userPrincipal.getUsername())
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                Diary diary = diaryService.getDiary(id, user).getData();
                return ResponseEntity.ok(ApiResponse.success(DiaryDto.from(diary)));
        }

        @Operation(summary = "사용자 일기 목록 조회", description = "현재 로그인한 사용자의 모든 일기를 조회합니다.")
        @GetMapping("/my")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<List<DiaryDto>>> getMyDiaries(
                @AuthenticationPrincipal UserPrincipal userPrincipal) {
                User user = userRepository.findByEmail(userPrincipal.getUsername())
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                List<Diary> diaries = diaryService.getUserDiaries(user).getData();
                List<DiaryDto> dtoList = diaries.stream()
                        .map(DiaryDto::from)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(ApiResponse.success(dtoList));
        }

        @Operation(summary = "일기 수정", description = "기존 일기를 수정합니다.")
        @PutMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<Diary>> updateDiary(
                        @Parameter(description = "일기 ID") @PathVariable Long id,
                        @Valid @RequestBody Diary updatedDiary,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                User user = userRepository.findByEmail(userPrincipal.getUsername())
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                return ResponseEntity.ok(diaryService.updateDiary(id, updatedDiary, user));
        }

        @Operation(summary = "일기 삭제", description = "일기를 삭제합니다.")
        @DeleteMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<Void>> deleteDiary(
                        @Parameter(description = "일기 ID") @PathVariable Long id,
                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
                User user = userRepository.findByEmail(userPrincipal.getUsername())
                                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
                return ResponseEntity.ok(diaryService.deleteDiary(id, user));
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

        // @Operation(summary = "기간별 일기 조회", description = "특정 기간의 일기를 조회합니다.")
        // @GetMapping("/period")
        // @PreAuthorize("isAuthenticated()")
        // public ResponseEntity<ApiResponse<List<Diary>>> getDiariesByPeriod(
        // @Parameter(description = "시작일") @RequestParam LocalDateTime start,
        // @Parameter(description = "종료일") @RequestParam LocalDateTime end,
        // @AuthenticationPrincipal UserPrincipal userPrincipal) {
        // User user = userRepository.findByEmail(userPrincipal.getUsername())
        // .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // return ResponseEntity.ok(diaryService.getUserDiariesByDateRange(user, start,
        // end));
        // }

}