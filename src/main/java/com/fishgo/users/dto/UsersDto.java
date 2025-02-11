package com.fishgo.users.dto;

import com.fishgo.users.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@PasswordMatches //Custom Annotation
public class UsersDto {

    private static final String ESSENTIAL = " 필수 입력 항목입니다.";


    @NotBlank(message = "아이디는" + ESSENTIAL)
    @Email(message = "아이디는 이메일 형식이어야 합니다.")
    private String userId;

    @NotBlank(message = "이름은" + ESSENTIAL)
    private String name;

    @NotBlank(message = "비밀번호는" + ESSENTIAL)
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 30자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은" + ESSENTIAL)
    private String confirmPassword;

    private String socialLoginInfo;

    private String role;

    private String profileImg;

}
