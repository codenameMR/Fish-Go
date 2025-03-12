package com.fishgo.posts.comments.service;

import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.domain.CommentLike;
import com.fishgo.posts.comments.repository.CommentLikeRepository;
import com.fishgo.posts.comments.repository.CommentRepository;
import com.fishgo.users.domain.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    // 좋아요 누르기
    public void likeComment(Long commentId, Users currentUser) {

        // 이미 좋아요 눌렀는지 확인 (중복 방지)
        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, currentUser.getId());
        if (alreadyLiked) {
            throw new IllegalStateException("이미 좋아요를 누른 댓글입니다.");
        }

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 좋아요 이력 생성
        CommentLike commentLike = CommentLike.builder()
                .comment(comment)
                .user(currentUser)
                .build();

        commentLikeRepository.save(commentLike);

        // (선택) Comment의 likeCount 증감
        comment.setLikeCount(comment.getLikeCount() + 1);
    }

    // 좋아요 취소
    public void unlikeComment(Long commentId, Users currentUser) {

        // 좋아요 이력 조회
        CommentLike commentLike = commentLikeRepository
                .findByCommentIdAndUserId(commentId, currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("좋아요를 누른 기록이 없습니다."));

        // DB에서 제거
        commentLikeRepository.delete(commentLike);

        // (선택) likeCount 감소
        Comment comment = commentLike.getComment();
        comment.setLikeCount(comment.getLikeCount() - 1);
    }
}