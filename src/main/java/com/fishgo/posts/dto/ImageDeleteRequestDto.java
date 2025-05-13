package com.fishgo.posts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ImageDeleteRequestDto {
    @Schema(description = "삭제할 이미지 ID 목록",
            example = "[1, 2, 3]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<Long> imageIds;

}
