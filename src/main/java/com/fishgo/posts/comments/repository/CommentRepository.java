package com.fishgo.posts.comments.repository;

import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentStatsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.post.id = :postId")
    List<Comment> findByPostIdWithReplies(@Param("postId") Long postId);

    @Query("SELECT NEW com.fishgo.posts.comments.dto.CommentStatsDto(" +
            "COUNT(c), COALESCE(SUM(c.likeCount), 0)) " +
            "FROM Comment c WHERE c.user.id = :userId")
    CommentStatsDto findCommentStatsByUserId(@Param("userId") long userId);
}
