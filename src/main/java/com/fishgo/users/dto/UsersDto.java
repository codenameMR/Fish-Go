package com.fishgo.users.dto;

import com.fishgo.users.validation.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Schema(description = "사용자 등록 및 인증 처리를 위한 DTO")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@PasswordMatches //Custom Annotation
public class UsersDto {

    private static final String ESSENTIAL = " 필수 입력 항목입니다.";

    @Schema(description = "사용자의 고유 아이디 (이메일 형식)", example = "test@naver.com")
    @NotBlank(message = "아이디는" + ESSENTIAL)
    @Email(message = "아이디는 이메일 형식이어야 합니다.")
    private String userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "이름은" + ESSENTIAL)
    private String name;

    @Schema(description = "비밀번호 (최소 8자 이상)", example = "testPW12#$")
    @NotBlank(message = "비밀번호는" + ESSENTIAL)
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 30자 이하여야 합니다.")
    private String password;

    @Schema(description = "비밀번호 확인", example = "testPW12#$")
    @NotBlank(message = "비밀번호 확인은" + ESSENTIAL)
    private String confirmPassword;

    @Schema(description = "소셜 로그인 정보", example = "google")
    private String socialLoginInfo;

    @Schema(description = "사용자 권한", example = "USER")
    private String role;

    @Schema(description = "사용자 프로필 이미지", example = "/images/profile.jpg")
    private String profileImg;

}
