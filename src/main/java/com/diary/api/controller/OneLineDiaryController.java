// package com.diary.api.controller;

// import com.diary.api.common.ApiResponse;
// import com.diary.api.domain.diary.entity.OneLineDiary;
// import com.diary.api.domain.diary.service.OneLineDiaryService;
// import com.diary.api.domain.user.entity.User;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.web.bind.annotation.*;

// import io.swagger.v3.oas.annotations.*;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import java.time.LocalDateTime;
// import java.util.List;

// @Slf4j
// @RestController
// @RequestMapping("/api/one-line-diaries")
// @RequiredArgsConstructor
// @Tag(name = "OneLineDiary", description = "한 줄 일기 관련 API")
// public class OneLineDiaryController {
// private final OneLineDiaryService oneLineDiaryService;

// @Operation(summary = "한 줄 일기 생성", description = "새로운 한 줄 일기를 생성합니다.")
// @PostMapping
// @PreAuthorize("isAuthenticated()")
// public ResponseEntity<ApiResponse<OneLineDiary>> createOneLineDiary(
// @Valid @RequestBody OneLineDiary oneLineDiary,
// @AuthenticationPrincipal User user) {
// return ResponseEntity.ok(oneLineDiaryService.createOneLineDiary(oneLineDiary,
// user));
// }

// @Operation(summary = "한 줄 일기 조회", description = "특정 한 줄 일기의 상세 정보를 조회합니다.")
// @GetMapping("/{id}")
// public ResponseEntity<ApiResponse<OneLineDiary>> getOneLineDiary(
// @Parameter(description = "한 줄 일기 ID") @PathVariable Long id,
// @AuthenticationPrincipal User user) {
// return ResponseEntity.ok(oneLineDiaryService.getOneLineDiary(id, user));
// }

// @Operation(summary = "한 줄 일기 수정", description = "기존 한 줄 일기를 수정합니다.")
// @PutMapping("/{id}")
// @PreAuthorize("isAuthenticated()")
// public ResponseEntity<ApiResponse<OneLineDiary>> updateOneLineDiary(
// @Parameter(description = "한 줄 일기 ID") @PathVariable Long id,
// @Valid @RequestBody OneLineDiary oneLineDiary,
// @AuthenticationPrincipal User user) {
// return ResponseEntity.ok(oneLineDiaryService.updateOneLineDiary(id,
// oneLineDiary, user));
// }

// @Operation(summary = "한 줄 일기 삭제", description = "한 줄 일기를 삭제합니다.")
// @DeleteMapping("/{id}")
// @PreAuthorize("isAuthenticated()")
// public ResponseEntity<ApiResponse<Void>> deleteOneLineDiary(
// @Parameter(description = "한 줄 일기 ID") @PathVariable Long id,
// @AuthenticationPrincipal User user) {
// return ResponseEntity.ok(oneLineDiaryService.deleteOneLineDiary(id, user));
// }

// @Operation(summary = "사용자 한 줄 일기 목록 조회", description = "특정 사용자의 한 줄 일기 목록을
// 조회합니다.")
// @GetMapping("/user")
// @PreAuthorize("isAuthenticated()")
// public ResponseEntity<ApiResponse<List<OneLineDiary>>> getUserOneLineDiaries(
// @AuthenticationPrincipal User user) {
// return ResponseEntity.ok(oneLineDiaryService.getUserOneLineDiaries(user));
// }

// @Operation(summary = "감정별 한 줄 일기 조회", description = "특정 감정의 한 줄 일기를 조회합니다.")
// @GetMapping("/emotion/{emotion}")
// public ResponseEntity<ApiResponse<List<OneLineDiary>>>
// getOneLineDiariesByEmotion(
// @Parameter(description = "감정") @PathVariable String emotion) {
// return
// ResponseEntity.ok(oneLineDiaryService.getOneLineDiariesByEmotion(emotion));
// }

// @Operation(summary = "태그별 한 줄 일기 조회", description = "특정 태그의 한 줄 일기를 조회합니다.")
// @GetMapping("/tags")
// public ResponseEntity<ApiResponse<List<OneLineDiary>>>
// getOneLineDiariesByTags(
// @RequestParam List<String> tags) {
// return ResponseEntity.ok(oneLineDiaryService.getOneLineDiariesByTags(tags));
// }

// @Operation(summary = "기간별 한 줄 일기 조회", description = "특정 기간의 한 줄 일기를 조회합니다.")
// @GetMapping("/period")
// @PreAuthorize("isAuthenticated()")
// public ResponseEntity<ApiResponse<List<OneLineDiary>>>
// getOneLineDiariesByPeriod(
// @Parameter(description = "시작일") @RequestParam LocalDateTime start,
// @Parameter(description = "종료일") @RequestParam LocalDateTime end,
// @AuthenticationPrincipal User user) {
// return
// ResponseEntity.ok(oneLineDiaryService.getUserOneLineDiariesByDateRange(user,
// start, end));
// }

// @Operation(summary = "분석 상태별 한 줄 일기 조회", description = "특정 분석 상태의 한 줄 일기를
// 조회합니다.")
// @GetMapping("/analysis-status/{status}")
// @PreAuthorize("isAuthenticated()")
// public ResponseEntity<ApiResponse<List<OneLineDiary>>>
// getOneLineDiariesByAnalysisStatus(
// @Parameter(description = "분석 상태") @PathVariable String status) {
// return
// ResponseEntity.ok(oneLineDiaryService.getOneLineDiariesByAnalysisStatus(status));
// }

// @Operation(summary = "사용자의 분석 상태별 한 줄 일기 조회", description = "현재 로그인한 사용자의 특정
// 분석 상태의 한 줄 일기를 조회합니다.")
// @GetMapping("/my/analysis-status/{status}")
// @PreAuthorize("isAuthenticated()")
// public ResponseEntity<ApiResponse<List<OneLineDiary>>>
// getMyOneLineDiariesByAnalysisStatus(
// @Parameter(description = "분석 상태") @PathVariable String status,
// @AuthenticationPrincipal User user) {
// return
// ResponseEntity.ok(oneLineDiaryService.getUserOneLineDiariesByAnalysisStatus(user,
// status));
// }
// }