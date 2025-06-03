package com.diary.api.infrastructure.user;

import com.diary.api.user.User;
import com.diary.api.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final EntityManager em;

    @Override
    public Optional<User> findByEmail(String email) {
        String jpql = "SELECT u FROM User u WHERE u.email = :email";
        List<User> result = em.createQuery(jpql, User.class)
                .setParameter("email", email)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Optional<User> findByNickname(String nickname) {
        String jpql = "SELECT u FROM User u WHERE u.nickname = :nickname";
        List<User> result = em.createQuery(jpql, User.class)
                .setParameter("nickname", nickname)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Optional<User> findByOauthId(String oauthId) {
        String jpql = "SELECT u FROM User u WHERE u.oauthId = :oauthId";
        List<User> result = em.createQuery(jpql, User.class)
                .setParameter("oauthId", oauthId)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Page<User> findByNicknameContaining(String nickname, Pageable pageable) {
        String jpql = "SELECT u FROM User u WHERE u.nickname LIKE :nickname";
        TypedQuery<User> query = em.createQuery(jpql, User.class)
                .setParameter("nickname", "%" + nickname + "%")
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        String countJpql = "SELECT COUNT(u) FROM User u WHERE u.nickname LIKE :nickname";
        Long total = em.createQuery(countJpql, Long.class)
                .setParameter("nickname", "%" + nickname + "%")
                .getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    public List<User> findByIsActiveTrue() {
        String jpql = "SELECT u FROM User u WHERE u.isActive = true";
        return em.createQuery(jpql, User.class)
                .getResultList();
    }

    @Override
    public List<User> findByLastLoginDateBefore(LocalDateTime date) {
        String jpql = "SELECT u FROM User u WHERE u.lastLoginDate < :date";
        return em.createQuery(jpql, User.class)
                .setParameter("date", date)
                .getResultList();
    }

    @Override
    public List<User> findByUserType(String userType) {
        String jpql = "SELECT u FROM User u WHERE u.userType = :userType";
        return em.createQuery(jpql, User.class)
                .setParameter("userType", userType)
                .getResultList();
    }

    @Override
    public List<User> findTopByOrderByDiaryCountDesc(int limit) {
        String jpql = "SELECT u FROM User u ORDER BY (SELECT COUNT(d) FROM Diary d WHERE d.user = u) DESC";
        return em.createQuery(jpql, User.class)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<User> findTopByOrderByCommunityParticipationDesc(int limit) {
        String jpql = "SELECT u FROM User u ORDER BY (SELECT COUNT(cm) FROM CommunityMember cm WHERE cm.user = u) DESC";
        return em.createQuery(jpql, User.class)
                .setMaxResults(limit)
                .getResultList();
    }
} 