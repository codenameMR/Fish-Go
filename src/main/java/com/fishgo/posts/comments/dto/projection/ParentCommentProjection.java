package com.fishgo.posts.comments.dto.projection;

import java.time.LocalDateTime;

public interface ParentCommentProjection {
    Long getCommentId();
    Long getUserId();
    String getContents();
    LocalDateTime getCreatedAt();
    Integer getLikeCount();
    String getName();
    String getProfileImg();

    Long getFirstReplyId();
    Integer getRemainingReplyCount();

}
