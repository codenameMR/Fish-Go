package com.fishgo.posts.dto;

import com.fishgo.common.util.ImagePathHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "게시글 범용 DTO")
public class PostsDto {

    @Schema(description = "게시글 아이디", example = "1")
    private Long id;

    @Schema(description = "사용자 아이디", example = "1")
    private Long userId;

    @Schema(description = "사용자 별명", example = "고등어#1234")
    private String userName;

    @Schema(description = "사용자 프로필 이미지", example = "/uploads/profile/{userId}/profile.png")
    private String userProfileImg;

    @Schema(description = "해시태그", example = "[\"#고등어\", \"맛집\"]")
    private List<String> hashtag;

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다.")
    @Schema(description = "제목", example = "고등어 낚시 다녀왔어요.")
    private String title;

    @Schema(description = "내용", example = "고등어 ~~~ 4짜 잡았어요 ~~~ ")
    private String contents;

    @Schema(description = "이미지", example = "[\"image/path/1.png\"]")
    private List<ImageDto> images;

    @Schema(description = "좋아요수", example = "23")
    private int likeCount;

    @Schema(description = "조회수", example = "33")
    private int viewCount;

    @Schema(description = "지역", example = "경기도 고양시")
    private String location;

    @Schema(description = "생선 종류", example = "고등어")
    private String fishType;

    @Schema(description = "생선 크기", example = "42.1")
    private float fishSize;

    @Schema(description = "위도", example = "37.65821589999999")
    private double lat;

    @Schema(description = "경도", example = "126.8320138")
    private double lon;

    @Schema(description = "게시글 생성 시간", example = "2025-04-03T18:12:27.212133816")
    private LocalDateTime createdAt;

    @Schema(description = "게시글 수정 시간", example = "2025-04-03T18:12:27.212133816")
    private LocalDateTime updatedAt;

    @Schema(description = "\"현재 사용자 기준\" 좋아요 여부")
    private boolean isLiked;

    public void setUserProfileImg(String profileImg) {
        this.userProfileImg = ImagePathHelper.buildProfileImagePath(profileImg, userId);
    }

}
