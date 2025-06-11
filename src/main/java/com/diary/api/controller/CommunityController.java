package com.diary.api.controller;

import com.diary.api.common.ApiResponse;
import com.diary.api.domain.community.dto.CommunityDTO;
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
import java.util.stream.Collectors;

@Tag(name = "Community", description = "커뮤니티 관련 API")
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @Operation(summary = "커뮤니티 생성", description = "새로운 커뮤니티를 생성합니다.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityDTO>> createCommunity(
            @Valid @RequestBody Community community,
            @AuthenticationPrincipal User user) {
        Community createdCommunity = communityService.createCommunity(community, user).getData();
        return ResponseEntity.ok(ApiResponse.success(CommunityDTO.from(createdCommunity)));
    }

    @Operation(summary = "커뮤니티 조회", description = "특정 커뮤니티의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommunityDTO>> getCommunity(
            @Parameter(description = "커뮤니티 ID") @PathVariable Long id) {
        Community community = communityService.getCommunity(id).getData();
        return ResponseEntity.ok(ApiResponse.success(CommunityDTO.from(community)));
    }

    @Operation(summary = "커뮤니티 수정", description = "커뮤니티 정보를 수정합니다.")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityDTO>> updateCommunity(
            @Parameter(description = "커뮤니티 ID") @PathVariable Long id,
            @Valid @RequestBody Community updateCommunity,
            @AuthenticationPrincipal User user) {
        Community updatedCommunity = communityService.updateCommunity(id, updateCommunity, user).getData();
        return ResponseEntity.ok(ApiResponse.success(CommunityDTO.from(updatedCommunity)));
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
    public ResponseEntity<ApiResponse<List<CommunityDTO>>> getDefaultCommunities() {
        List<Community> communities = communityService.getDefaultCommunities().getData();
        List<CommunityDTO> communityDTOs = communities.stream()
                .map(CommunityDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(communityDTOs));
    }

    @Operation(summary = "감정 테마별 커뮤니티 조회", description = "특정 감정 테마의 커뮤니티 목록을 조회합니다.")
    @GetMapping("/emotion/{emotionTheme}")
    public ResponseEntity<ApiResponse<List<CommunityDTO>>> getCommunitiesByEmotionTheme(
            @Parameter(description = "감정 테마") @PathVariable String emotionTheme) {
        List<Community> communities = communityService.getCommunitiesByEmotionTheme(emotionTheme).getData();
        List<CommunityDTO> communityDTOs = communities.stream()
                .map(CommunityDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(communityDTOs));
    }

    @Operation(summary = "커뮤니티 검색", description = "키워드와 감정 테마로 커뮤니티를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CommunityDTO>>> searchCommunities(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "감정 테마") @RequestParam(required = false) String emotionTheme) {
        List<Community> communities = communityService.searchCommunities(keyword, emotionTheme).getData();
        List<CommunityDTO> communityDTOs = communities.stream()
                .map(CommunityDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(communityDTOs));
    }

    @Operation(summary = "사용자의 커뮤니티 목록 조회", description = "현재 로그인한 사용자가 참여한 커뮤니티 목록을 조회합니다.")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CommunityDTO>>> getUserCommunities(
            @AuthenticationPrincipal User user) {
        List<Community> communities = communityService.getUserCommunities(user).getData();
        List<CommunityDTO> communityDTOs = communities.stream()
                .map(CommunityDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(communityDTOs));
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