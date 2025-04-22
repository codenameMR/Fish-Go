package com.fishgo.posts.comments.dto;

import com.fishgo.posts.comments.domain.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentWithFirstReplyResponseDto extends CommentResponseDto{

    @Schema(description = "첫번째 대댓글 필드")
    private CommentResponseDto firstReply;

    @Schema(description = "남은 대댓글 개수")
    private int remainingReplyCount;

    @Schema(description = "댓글 상태", example = "ACTIVE / USER_WITHDRAWN / DELETED_BY_USER / DELETED_BY_ADMIN")
    private CommentStatus status = CommentStatus.ACTIVE;

}
