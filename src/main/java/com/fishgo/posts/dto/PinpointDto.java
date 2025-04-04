package com.fishgo.posts.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PinpointDto {

    private Long postId;

    private Double lat;

    private Double lon;

}
