package com.fishgo.users.repository;

import com.fishgo.users.domain.UserStatus;
import com.fishgo.users.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findById(long userId);

    Optional<Users> findByEmail(String userId);

    boolean existsByEmail(String userId);

    boolean existsByProfile_Name(String name);

    Optional<Users> findByProfile_Name(String name);

    List<Users> findByWithdrawRequestedAtBeforeAndStatus(
            LocalDateTime dateTime,
            UserStatus status
    );


}
