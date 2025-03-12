package com.fishgo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserStatsDto {
    private String name;
    private String profileImg;
    private long postCount;       // 게시글 수
    private long commentCount;    // 댓글 수
    private long likeCount;           // 좋아요 수

}
