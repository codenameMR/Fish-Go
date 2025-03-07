package com.fishgo.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "로그인 DTO")
@Setter
@Getter
public class LoginRequestDto {

    @Schema(description = "사용자의 고유 아이디 (이메일 형식)", example = "test@naver.com")
    private String email;

    @Schema(description = "비밀번호 (최소 8자 이상)", example = "testPW12#$")
    private String password;

}
