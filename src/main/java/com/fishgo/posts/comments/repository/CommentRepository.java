package com.fishgo.posts.comments.repository;

import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // (1) 부모만 페이징 조회: replies는 fetch join하지 않음
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL")
    Page<Comment> findByPostIdWithoutReplies(@Param("postId") Long postId, Pageable pageable);

    // (2) 부모와 replies를 fetch join (부모 IDs를 기준으로 IN 조회)
    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.replies " +
            "WHERE c.id IN :commentIds")
    List<Comment> findCommentsWithReplies(@Param("commentIds") List<Long> commentIds);

    @Query("SELECT NEW com.fishgo.posts.comments.dto.CommentStatsDto(" +
            "COUNT(c), COALESCE(SUM(c.likeCount), 0)) " +
            "FROM Comment c WHERE c.user.id = :userId")
    CommentStatsDto findCommentStatsByUserId(@Param("userId") long userId);
}
