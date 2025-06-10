package com.diary.api.controller;

import com.diary.api.common.ApiResponse;
import com.diary.api.domain.community.entity.Community;
import com.diary.api.domain.community.service.CommunityService;
import com.diary.api.domain.user.entity.User;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Community", description = "커뮤니티 관련 API")
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @Operation(summary = "커뮤니티 생성", description = "새로운 커뮤니티를 생성합니다.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Community>> createCommunity(
            @Valid @RequestBody Community community,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(communityService.createCommunity(community, user));
    }

    @Operation(summary = "커뮤니티 조회", description = "특정 커뮤니티의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Community>> getCommunity(
            @Parameter(description = "커뮤니티 ID") @PathVariable Long id) {
        return ResponseEntity.ok(communityService.getCommunity(id));
    }

    @Operation(summary = "커뮤니티 수정", description = "기존 커뮤니티를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Community>> updateCommunity(
            @Parameter(description = "커뮤니티 ID") @PathVariable Long id,
            @Valid @RequestBody Community community,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(communityService.updateCommunity(id, community, user));
    }

    @Operation(summary = "커뮤니티 삭제", description = "커뮤니티를 삭제합니다.")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteCommunity(
            @Parameter(description = "커뮤니티 ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(communityService.deleteCommunity(id, user));
    }

    @Operation(summary = "기본 커뮤니티 목록 조회", description = "기본 커뮤니티 목록을 조회합니다.")
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<List<Community>>> getDefaultCommunities() {
        return ResponseEntity.ok(communityService.getDefaultCommunities());
    }

    @Operation(summary = "감정 테마별 커뮤니티 조회", description = "특정 감정 테마의 커뮤니티를 조회합니다.")
    @GetMapping("/emotion-theme/{emotionTheme}")
    public ResponseEntity<ApiResponse<List<Community>>> getCommunitiesByEmotionTheme(
            @Parameter(description = "감정 테마") @PathVariable String emotionTheme) {
        return ResponseEntity.ok(communityService.getCommunitiesByEmotionTheme(emotionTheme));
    }

    @Operation(summary = "커뮤니티 검색", description = "커뮤니티를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Community>>> searchCommunities(
            @Parameter(description = "검색어") @RequestParam String keyword,
            @Parameter(description = "감정 테마") @RequestParam(required = false) String emotionTheme) {
        return ResponseEntity.ok(communityService.searchCommunities(keyword, emotionTheme));
    }

    @Operation(summary = "사용자의 커뮤니티 목록 조회", description = "현재 로그인한 사용자가 참여 중인 커뮤니티 목록을 조회합니다.")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Community>>> getUserCommunities(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(communityService.getUserCommunities(user));
    }

    @Operation(summary = "커뮤니티 가입", description = "커뮤니티에 가입합니다.")
    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> joinCommunity(
            @Parameter(description = "커뮤니티 ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(communityService.joinCommunity(id, user));
    }

    @Operation(summary = "커뮤니티 탈퇴", description = "커뮤니티에서 탈퇴합니다.")
    @PostMapping("/{id}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> leaveCommunity(
            @Parameter(description = "커뮤니티 ID") @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(communityService.leaveCommunity(id, user));
    }
}