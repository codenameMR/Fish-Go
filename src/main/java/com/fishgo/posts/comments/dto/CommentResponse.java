package com.fishgo.posts.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponse {

    @Schema(description = "작성자 email")
    private String email;

    @Schema(description = "댓글 내용")
    private String contents;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "부모 댓글 ID", nullable = true)
    private Long parentId;

    @Schema(description = "신고 횟수")
    private Long reportCount;

}
