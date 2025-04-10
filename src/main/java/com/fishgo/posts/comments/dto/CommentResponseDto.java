package com.fishgo.posts.comments.dto;

import com.fishgo.common.util.ImagePathHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentResponseDto {

    @Schema(description = "댓글 고유 ID")
    private Long id;

    @Schema(description = "사용자 아이디")
    private long userId;

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
    private String profileImg;

    @Schema(description = "첫번째 대댓글 필드")
    private ReplyResponseDto firstReply;

    @Schema(description = "남은 대댓글 개수")
    private int remainingReplyCount;


    public void setProfileImg(String profileImg) {
        this.profileImg = ImagePathHelper.buildProfileImagePath(profileImg, userId);
    }
}
