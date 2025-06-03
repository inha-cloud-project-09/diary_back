package com.diary.api.community.controller;

import com.diary.api.community.Community;
import com.diary.api.community.service.CommunityService;
import com.diary.api.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clusters")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getClusters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        return ResponseEntity.ok(communityService.getClustersWithFilters(pageRequest, category, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCluster(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getClusterWithDetails(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCluster(
            @AuthenticationPrincipal User user,
            @RequestBody Community community) {
        community.setCreator(user);
        return ResponseEntity.ok(communityService.createCluster(community));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCluster(
            @PathVariable Long id,
            @RequestBody Community updatedCluster) {
        return ResponseEntity.ok(communityService.updateCluster(id, updatedCluster));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteCluster(@PathVariable Long id) {
        communityService.deleteCluster(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Map<String, Object>> joinCluster(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(communityService.joinCluster(user, id));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Map<String, Object>> leaveCluster(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(communityService.leaveCluster(user, id));
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<Map<String, Object>>> getRecommendedClusters(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(communityService.getRecommendedClusters(user));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<Map<String, Object>>> getTrendingClusters() {
        return ResponseEntity.ok(communityService.getTrendingClusters());
    }
} 