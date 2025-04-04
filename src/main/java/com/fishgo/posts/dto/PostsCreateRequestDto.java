package com.fishgo.posts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "게시글 생성 요청 DTO")
@Getter
@Setter
public class PostsCreateRequestDto {

    @Schema(description = "제목")
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다.")
    private String title;

    @Schema(description = "내용")
    private String contents;

    @Schema(description = "해시태그 리스트")
    private List<String> hashTag;

    @Schema(description = "장소")
    private String location;

    @Schema(description = "물고기 종류")
    private String fishType;

    @Schema(description = "물고기 크기")
    private float fishSize;

    @Schema(description = "위도")
    private double lat;

    @Schema(description = "경도")
    private double lon;

}
