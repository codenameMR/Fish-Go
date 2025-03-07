package com.fishgo.posts.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CommentResponseDto {

    @Schema(description = "댓글 고유 ID")
    private Long id;

    @Schema(description = "작성자 이름")
    private String name;

    @Schema(description = "댓글 내용")
    private String contents;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "부모 댓글 ID", nullable = true)
    private Long parentId;

    @Schema(description = "좋아요 수")
    private Long likeCount;

    @Schema(description = "\"현재 사용자 기준\" 좋아요 여부")
    private boolean isLiked;

    @Schema(description = "프로필 이미지")
    private Long profileImg;

    @Schema(description = "대댓글 필드")
    private List<CommentResponseDto> replies;




}
