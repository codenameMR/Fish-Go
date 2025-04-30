package com.fishgo.badge.repository;

import com.fishgo.badge.domain.UserBadge;
import com.fishgo.users.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUser(Users user);

    @Query("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.user.id = :userId")
    List<UserBadge> findByUserIdWithBadge(@Param("userId") Long userId);

    boolean existsByUserAndBadge_Code(Users user, String badgeCode);

    Optional<UserBadge> findByUserAndBadge_Code(Users user, String badgeCode);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.user.id = :userId")
    long countBadgesByUserId(@Param("userId") Long userId);
}
