package com.fishgo.posts.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "댓글 작성 요청 DTO")
public class CommentCreateRequestDto {

    @Schema(description = "작성 대상 게시글의 식별자(ID)")
    private Long postId;

    @Schema(description = "댓글 내용", example = "내용을 입력하세요.")
    private String contents;

    @Schema(description = "부모 댓글 ID (대댓글 작성 시 사용)", nullable = true)
    private Long parentId;

    @Schema(description = "멘션된 유저 아이디", nullable = true)
    private Long mentionedUserId;
}