package com.fishgo.posts.comments.dto.projection;

import com.fishgo.posts.comments.domain.CommentStatus;

import java.time.LocalDateTime;

public interface ParentCommentProjection {
    Long getCommentId();
    Long getUserId();
    String getContents();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Integer getLikeCount();
    String getName();
    String getProfileImg();
    CommentStatus getStatus();

    Long getFirstReplyId();
    Integer getRemainingReplyCount();

    // 단일 멘션 (JSON 문자열로 가져옴)
    String getMentionedUser();


}
