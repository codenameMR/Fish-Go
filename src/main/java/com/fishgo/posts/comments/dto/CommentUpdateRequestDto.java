package com.fishgo.posts.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "댓글 업데이트 요청 DTO")
public class CommentUpdateRequestDto {

    @Schema(description = "수정할 댓글 ID")
    private Long commentId;

    @Schema(description = "댓글 내용 (수정 후 내용)", example = "수정 내용 입력")
    private String contents;

    @Schema(description = "멘션된 유저 아이디 (수정 사항이 없어도 기존 아이디를 보내야함.)", nullable = true)
    private Long mentionedUserId;
}