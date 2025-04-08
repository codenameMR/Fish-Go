package com.fishgo.posts.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fishgo.common.util.ImagePathHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "게시글 응답 DTO")
@Getter
@Setter
public class PostListResponseDto {

    private Long id;

    private String userProfileImg;

    @JsonIgnore
    private long userId;

    private String userName;

    private String title;

    private String contents;

    private String thumbnail;

    private int likeCount;

    private int viewCount;

    private LocalDateTime createdAt;

    public void setUserProfileImg(String profileImg) {
        this.userProfileImg = ImagePathHelper.buildProfileImagePath(profileImg, userId);
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = ImagePathHelper.buildPostImagePath(thumbnail, this.id);
    }
}
