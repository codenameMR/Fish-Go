package com.fishgo.users.dto;

import com.fishgo.common.util.ImagePathHelper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatsDto {
    private long id;
    private String name;
    private String profileImg;
    private long postCount;       // 게시글 수
    private long commentCount;    // 댓글 수
    private long likeCount;           // 좋아요 수

    public void setProfileImg(String profileImg) {
        this.profileImg = ImagePathHelper.buildProfileImagePath(profileImg, id);
    }

    public UserStatsDto(long id, String name, String profileImg, long postCount, long commentCount, long likeCount) {
        this.id = id;
        this.name = name;
        this.profileImg = ImagePathHelper.buildProfileImagePath(profileImg, id);
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;

    }
}
