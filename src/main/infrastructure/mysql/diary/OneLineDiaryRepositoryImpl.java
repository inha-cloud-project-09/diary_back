package com.diary.api.infrastructure.diary;

import com.diary.api.diary.OneLineDiary;
import com.diary.api.diary.repository.OneLineDiaryRepository;
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

@Repository
@RequiredArgsConstructor
public class OneLineDiaryRepositoryImpl implements OneLineDiaryRepository {
    private final EntityManager em;

    @Override
    public Page<OneLineDiary> findByUser(User user, Pageable pageable) {
        String jpql = "SELECT d FROM OneLineDiary d WHERE d.user = :user";
        TypedQuery<OneLineDiary> query = em.createQuery(jpql, OneLineDiary.class)
                .setParameter("user", user)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        String countJpql = "SELECT COUNT(d) FROM OneLineDiary d WHERE d.user = :user";
        Long total = em.createQuery(countJpql, Long.class)
                .setParameter("user", user)
                .getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    public List<OneLineDiary> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end) {
        String jpql = "SELECT d FROM OneLineDiary d WHERE d.user = :user AND d.createdAt BETWEEN :start AND :end";
        return em.createQuery(jpql, OneLineDiary.class)
                .setParameter("user", user)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    @Override
    public List<OneLineDiary> findByIsPublicTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        String jpql = "SELECT d FROM OneLineDiary d WHERE d.isPublic = true AND d.createdAt BETWEEN :start AND :end";
        return em.createQuery(jpql, OneLineDiary.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    @Override
    public List<OneLineDiary> findByAnalysisStatus(String status) {
        String jpql = "SELECT d FROM OneLineDiary d WHERE d.analysisStatus = :status";
        return em.createQuery(jpql, OneLineDiary.class)
                .setParameter("status", status)
                .getResultList();
    }

    public List<OneLineDiary> findByTagsContaining(List<String> tags) {
        String jpql = "SELECT d FROM OneLineDiary d WHERE d.tags IN :tags";
        return em.createQuery(jpql, OneLineDiary.class)
                .setParameter("tags", tags)
                .getResultList();
    }

    public List<OneLineDiary> findByPrimaryEmotion(String emotion) {
        String jpql = "SELECT d FROM OneLineDiary d WHERE d.primaryEmotion = :emotion";
        return em.createQuery(jpql, OneLineDiary.class)
                .setParameter("emotion", emotion)
                .getResultList();
    }
} 