package com.fishgo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserOverviewDto {
    private String name;
    private String profileImg;
    private int postCount;       // 게시글 수
    private int commentCount;    // 댓글 수
    private int likeCount;           // 좋아요 수

}
