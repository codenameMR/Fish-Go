package com.fishgo.posts.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUpdateRequest {

    @Schema(description = "수정할 댓글 ID")
    private Long commentId;

    @Schema(description = "댓글 내용 (수정 후 내용)", example = "수정 내용 입력")
    private String contents;
}