package com.diary.api.infrastructure.community;

import com.diary.api.community.Community;
import com.diary.api.community.repository.CommunityRepository;
import com.diary.api.user.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommunityRepositoryImpl implements CommunityRepository {
    private final EntityManager em;

    @Override
    public Page<Community> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable) {
        String jpql = "SELECT c FROM Community c WHERE c.name LIKE :name OR c.description LIKE :description";
        TypedQuery<Community> query = em.createQuery(jpql, Community.class)
                .setParameter("name", "%" + name + "%")
                .setParameter("description", "%" + description + "%")
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        String countJpql = "SELECT COUNT(c) FROM Community c WHERE c.name LIKE :name OR c.description LIKE :description";
        Long total = em.createQuery(countJpql, Long.class)
                .setParameter("name", "%" + name + "%")
                .setParameter("description", "%" + description + "%")
                .getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    public List<Community> findByCreator(User creator) {
        String jpql = "SELECT c FROM Community c WHERE c.creator = :creator";
        return em.createQuery(jpql, Community.class)
                .setParameter("creator", creator)
                .getResultList();
    }

    @Override
    public List<Community> findByEmotionTheme(String emotionTheme) {
        String jpql = "SELECT c FROM Community c WHERE c.emotionTheme = :emotionTheme";
        return em.createQuery(jpql, Community.class)
                .setParameter("emotionTheme", emotionTheme)
                .getResultList();
    }

    @Override
    public List<Community> findByIsDefaultTrue() {
        String jpql = "SELECT c FROM Community c WHERE c.isDefault = true";
        return em.createQuery(jpql, Community.class)
                .getResultList();
    }

    public List<Community> findRecommendedCommunities(User user) {
        // 사용자의 감정 벡터와 유사한 감정 테마를 가진 커뮤니티 검색
        String jpql = "SELECT c FROM Community c WHERE c.emotionTheme IN " +
                     "(SELECT d.primaryEmotion FROM Diary d WHERE d.user = :user)";
        return em.createQuery(jpql, Community.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Community> findTrendingCommunities() {
        // 최근 활동이 많은 커뮤니티 검색
        String jpql = "SELECT c FROM Community c ORDER BY " +
                     "(SELECT COUNT(cm) FROM CommunityMember cm WHERE cm.community = c AND cm.isActive = true) DESC";
        return em.createQuery(jpql, Community.class)
                .setMaxResults(10)
                .getResultList();
    }
} 