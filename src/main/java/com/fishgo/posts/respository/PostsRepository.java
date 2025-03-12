package com.fishgo.posts.respository;

import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostStatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostsRepository extends JpaRepository<Posts, Long> {

    @Query(
            "SELECT p FROM Posts p WHERE " +
            "(:title IS NULL OR p.title LIKE %:title%) AND " +
            "(:fishType IS NULL OR p.fishType LIKE %:fishType%)"
        )
    List<Posts> searchPosts(@Param("title") String title, @Param("fishType") String fishType);

    @Query("SELECT NEW com.fishgo.posts.dto.PostStatsDto(" +
            "COUNT(p), COALESCE(SUM(p.likeCount), 0)) " +
            "FROM Posts p WHERE p.users.id = :userId")
    PostStatsDto findPostStatsByUserId(@Param("userId") long userId);


}
