package com.fishgo.posts.comments.repository;

import com.fishgo.posts.comments.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 특정 댓글 + 사용자 조합(좋아요 이력) 검색 메서드
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    // “이미 좋아요가 눌린 상태인지?” 확인할 때 사용
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
}
