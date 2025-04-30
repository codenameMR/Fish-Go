package com.fishgo.badge.event;

import com.fishgo.posts.comments.domain.Comment;

/**
 * 댓글 작성 이벤트
 */
public record CommentCreatedEvent(Comment comment) {
}
