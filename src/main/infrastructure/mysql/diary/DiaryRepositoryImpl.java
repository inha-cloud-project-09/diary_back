package com.diary.api.infrastructure.diary;

import com.diary.api.diary.Diary;
import com.diary.api.diary.repository.DiaryRepository;
import com.diary.api.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepository {
    private final EntityManager em;

    @Override
    public Page<Diary> findByUser(User user, Pageable pageable) {
        String jpql = "SELECT d FROM Diary d WHERE d.user = :user";
        TypedQuery<Diary> query = em.createQuery(jpql, Diary.class)
                .setParameter("user", user)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        String countJpql = "SELECT COUNT(d) FROM Diary d WHERE d.user = :user";
        Long total = em.createQuery(countJpql, Long.class)
                .setParameter("user", user)
                .getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    public List<Diary> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end) {
        String jpql = "SELECT d FROM Diary d WHERE d.user = :user AND d.createdAt BETWEEN :start AND :end";
        return em.createQuery(jpql, Diary.class)
                .setParameter("user", user)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    @Override
    public List<Diary> findByIsPublicTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        String jpql = "SELECT d FROM Diary d WHERE d.isPublic = true AND d.createdAt BETWEEN :start AND :end";
        return em.createQuery(jpql, Diary.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    @Override
    public List<Diary> findByAnalysisStatus(String status) {
        String jpql = "SELECT d FROM Diary d WHERE d.analysisStatus = :status";
        return em.createQuery(jpql, Diary.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Diary> findByPrimaryEmotion(String emotion) {
        String jpql = "SELECT d FROM Diary d WHERE d.primaryEmotion = :emotion";
        return em.createQuery(jpql, Diary.class)
                .setParameter("emotion", emotion)
                .getResultList();
    }

    public List<Diary> findByTagsContaining(List<String> tags) {
        String jpql = "SELECT d FROM Diary d WHERE d.tags IN :tags";
        return em.createQuery(jpql, Diary.class)
                .setParameter("tags", tags)
                .getResultList();
    }

    public List<Diary> findByEmotionVector(Map<String, Double> emotionVector) {
        // 감정 벡터 기반 유사도 검색 구현
        String jpql = "SELECT d FROM Diary d WHERE d.emotionVector = :emotionVector";
        return em.createQuery(jpql, Diary.class)
                .setParameter("emotionVector", emotionVector)
                .getResultList();
    }
} 