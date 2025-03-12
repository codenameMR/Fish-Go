package com.fishgo.users.repository;

import com.fishgo.users.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query("SELECT p.profileImg FROM Profile p WHERE p.user.id = :userId")
    String findProfileImgByUserId(@Param("userId") Long userId);

    boolean existsByName(String name);
}
