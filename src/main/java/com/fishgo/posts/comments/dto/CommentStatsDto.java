package com.fishgo.posts.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CommentStatsDto {
    private long commentCount;
    private long totalLikes;
}
