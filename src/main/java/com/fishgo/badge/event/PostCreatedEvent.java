package com.fishgo.badge.event;

import com.fishgo.posts.domain.Posts;

/**
 * 게시글 작성 이벤트
 */
public record PostCreatedEvent(Posts post) {
}