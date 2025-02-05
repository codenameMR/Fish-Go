package com.fishgo.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class UsersDto {

    private static final String ESSENTIAL = " 필수 입력 항목입니다.";

    @NotBlank(message = "아이디는" + ESSENTIAL)
    @Size(min = 5, max = 20, message = "아이디는 5자 이상 20자 이하여야 합니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9]{5,20}$",
            message = "아이디는 영문과 숫자만 사용 가능합니다."
    )
    private String userId;

    @NotBlank(message = "이름은" + ESSENTIAL)
    private String name;

    @NotBlank(message = "비밀번호는" + ESSENTIAL)
    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    private String socialLoginInfo;

    private String role;

    private String profileImg;

}
