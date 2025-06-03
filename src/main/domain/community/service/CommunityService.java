package com.diary.service;

import com.diary.domain.Community;
import com.diary.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final CommunityRepository communityRepository;

    @Transactional
    public Community createPost(Community community) {
        return communityRepository.save(community);
    }

    public Community getPost(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public Page<Community> getAllPosts(Pageable pageable) {
        return communityRepository.findAll(pageable);
    }

    public Page<Community> searchPosts(String keyword, Pageable pageable) {
        return communityRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }

    public List<Community> getPostsByAuthor(String author) {
        return communityRepository.findByAuthor(author);
    }

    public List<Community> getPostsByDateRange(LocalDateTime start, LocalDateTime end) {
        return communityRepository.findByCreatedAtBetween(start, end);
    }

    public List<Community> getPopularPostsByViews(Pageable pageable) {
        return communityRepository.findAllByOrderByViewCountDesc(pageable);
    }

    public List<Community> getPopularPostsByLikes(Pageable pageable) {
        return communityRepository.findAllByOrderByLikeCountDesc(pageable);
    }

    @Transactional
    public Community updatePost(Long id, Community updatedPost) {
        Community post = getPost(id);
        post.setTitle(updatedPost.getTitle());
        post.setContent(updatedPost.getContent());
        return communityRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        communityRepository.deleteById(id);
    }

    @Transactional
    public Community incrementViewCount(Long id) {
        Community post = getPost(id);
        post.setViewCount(post.getViewCount() + 1);
        return communityRepository.save(post);
    }

    @Transactional
    public Community incrementLikeCount(Long id) {
        Community post = getPost(id);
        post.setLikeCount(post.getLikeCount() + 1);
        return communityRepository.save(post);
    }
} 