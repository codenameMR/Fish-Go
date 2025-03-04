package com.fishgo.posts.comments.repository;

import com.fishgo.posts.comments.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.post.id = :postId AND c.parent IS NULL")
    List<Comment> findByPostIdWithReplies(@Param("postId") Long postId);

}
