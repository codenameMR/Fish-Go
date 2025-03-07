package com.fishgo.users.repository;

import com.fishgo.users.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findById(long userId);
    Optional<Users> findByEmail(String userId);
    boolean existsByEmail(String userId);
    boolean existsByName(String name);
    void deleteById(long userId);
}
