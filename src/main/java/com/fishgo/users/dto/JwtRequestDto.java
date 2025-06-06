package com.fishgo.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "JWT 생성시 요구 되는 DTO")
@Getter
@Setter
public class JwtRequestDto {

    @Schema(description = "DB에서 생성되는 고유 ID",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "사용자의 고유 아이디 (이메일 형식)", example = "test@naver.com")
    private String email;

    @Schema(description = "사용자 권한", accessMode = Schema.AccessMode.READ_ONLY)
    private String role;
}
