package com.diary.controller;

import com.diary.domain.Community;
import com.diary.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<Community> createPost(@RequestBody Community community) {
        return ResponseEntity.ok(communityService.createPost(community));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Community> getPost(@PathVariable Long id) {
        communityService.incrementViewCount(id);
        return ResponseEntity.ok(communityService.getPost(id));
    }

    @GetMapping
    public ResponseEntity<Page<Community>> getAllPosts(Pageable pageable) {
        return ResponseEntity.ok(communityService.getAllPosts(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Community>> searchPosts(
            @RequestParam String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(communityService.searchPosts(keyword, pageable));
    }

    @GetMapping("/author/{author}")
    public ResponseEntity<List<Community>> getPostsByAuthor(@PathVariable String author) {
        return ResponseEntity.ok(communityService.getPostsByAuthor(author));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Community>> getPostsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(communityService.getPostsByDateRange(start, end));
    }

    @GetMapping("/popular/views")
    public ResponseEntity<List<Community>> getPopularPostsByViews(Pageable pageable) {
        return ResponseEntity.ok(communityService.getPopularPostsByViews(pageable));
    }

    @GetMapping("/popular/likes")
    public ResponseEntity<List<Community>> getPopularPostsByLikes(Pageable pageable) {
        return ResponseEntity.ok(communityService.getPopularPostsByLikes(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Community> updatePost(
            @PathVariable Long id,
            @RequestBody Community updatedPost) {
        return ResponseEntity.ok(communityService.updatePost(id, updatedPost));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        communityService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Community> likePost(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.incrementLikeCount(id));
    }
} 