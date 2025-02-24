package com.fishgo.posts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class PostsDto {
    private static final String ESSENTIAL = " 필수 입력 항목입니다.";

    private Long userId;

    private String hashTag;

    @NotBlank(message = "제목은" + ESSENTIAL)
    @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다.")
    private String title;

    private String contents;

    private String img;

    private Map<String, Object> metaData;

    private int reportCount;

    private boolean isActive;

    private int likeCount;

    private int viewCount;

    private String location;

    private String fishType;

    private float fishSize;

}
