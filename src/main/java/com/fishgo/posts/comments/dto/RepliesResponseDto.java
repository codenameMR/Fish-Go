package com.fishgo.posts.comments.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
public class RepliesResponseDto {

    private long remainingRepliesCount;

    private Page<ReplyResponseDto> replies;

}
