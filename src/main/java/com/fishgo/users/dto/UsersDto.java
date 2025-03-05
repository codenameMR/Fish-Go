package com.fishgo.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Schema(description = "사용자 등록 및 인증 처리를 위한 DTO")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class UsersDto {

    @Schema(description = "DB에서 생성되는 고유 ID",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "사용자의 고유 아이디 (이메일 형식)", example = "test@naver.com")
    private String email;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "비밀번호 (최소 8자 이상)", example = "testPW12#$")
    private String password;

    @Schema(description = "비밀번호 확인", example = "testPW12#$")
    private String confirmPassword;

    @Schema(description = "소셜 로그인 정보")
    private String socialLoginInfo;

    @Schema(description = "사용자 권한", accessMode = Schema.AccessMode.READ_ONLY)
    private String role;

    @Schema(description = "사용자 프로필 이미지")
    private String profileImg;

}
