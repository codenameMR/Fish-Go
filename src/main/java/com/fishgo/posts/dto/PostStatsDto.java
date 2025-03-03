package com.fishgo.posts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostStatsDto {
    private long postCount;
    private long totalLikes;
}
