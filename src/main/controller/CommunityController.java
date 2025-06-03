package com.diary.api.community.controller;

import com.diary.api.common.ApiResponse;
import com.diary.api.community.Community;
import com.diary.api.community.service.CommunityService;
import com.diary.api.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Community", description = "커뮤니티 관련 API")
@RestController
@RequestMapping("/api/clusters")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @Operation(summary = "커뮤니티 생성", description = "새로운 커뮤니티를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Community>> createCluster(
            @AuthenticationPrincipal User user,
            @RequestBody Community community) {
        community.setCreator(user);
        return ResponseEntity.ok(communityService.createCluster(community));
    }

    @Operation(summary = "커뮤니티 조회", description = "특정 커뮤니티의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Community>> getCluster(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getClusterWithDetails(id));
    }

    @Operation(summary = "커뮤니티 수정", description = "기존 커뮤니티를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Community>> updateCluster(
            @Parameter(description = "커뮤니티 ID") @PathVariable Long id,
            @RequestBody Community updatedCluster) {
        return ResponseEntity.ok(communityService.updateCluster(id, updatedCluster));
    }

    @Operation(summary = "커뮤니티 삭제", description = "커뮤니티를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCluster(@PathVariable Long id) {
        communityService.deleteCluster(id);
        return ResponseEntity.ok(ApiResponse.of(true));
    }

    @Operation(summary = "커뮤니티 가입", description = "커뮤니티에 가입합니다.")
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<Void>> joinCluster(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(communityService.joinCluster(user, id));
    }

    @Operation(summary = "커뮤니티 탈퇴", description = "커뮤니티에서 탈퇴합니다.")
    @PostMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveCluster(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(communityService.leaveCluster(user, id));
    }

    @Operation(summary = "커뮤니티 목록 조회", description = "커뮤니티 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Community>>> getClusters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        return ResponseEntity.ok(communityService.getClustersWithFilters(pageRequest, category, search));
    }

    @Operation(summary = "커뮤니티 검색", description = "커뮤니티를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Community>>> searchClusters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String emotionTheme,
            @RequestParam(required = false) String creator) {
        return ResponseEntity.ok(communityService.searchClusters(name, emotionTheme, creator));
    }

    @Operation(summary = "커뮤니티 추천", description = "커뮤니티를 추천합니다.")
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<Community>>> getRecommendedClusters(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(communityService.getRecommendedClusters(user));
    }

    @Operation(summary = "커뮤니티 인기", description = "인기 있는 커뮤니티를 조회합니다.")
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<Community>>> getTrendingClusters() {
        return ResponseEntity.ok(communityService.getTrendingClusters());
    }
} 