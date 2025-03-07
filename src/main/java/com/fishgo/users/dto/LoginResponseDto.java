package com.fishgo.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "로그인 응답 DTO")
@Getter
@Setter
public class LoginResponseDto {

    @Schema(description = "사용자 정보")
    UserResponseDto user;

    @Schema(description = "액세스 토큰")
    String accessToken;
}
