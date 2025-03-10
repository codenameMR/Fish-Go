package com.fishgo.posts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostsDto {

    private Long id;

    private String userName;

    private List<String> hashtag;

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다.")
    private String title;

    private String contents;

    private List<ImageDto> images;

    private int likeCount;

    private int viewCount;

    private String location;

    private String fishType;

    private float fishSize;

    private LocalDateTime createdAt;

    private Boolean isModify;

    private double lat;

    private double lon;

}
