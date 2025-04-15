package com.fishgo.posts.comments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "멘션 된 유저 정보용 DTO")
@Setter
@Getter
public class CommentMentionDto {

    @Schema(description = "멘션 된 유저 아이디")
    private Long id;

    @Schema(description = "멘션 된 유저 닉네임")
    private String name;

}
