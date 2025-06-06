package com.fishgo.users.dto;

import com.fishgo.common.util.ImagePathHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto {

    @Schema(description = "DB에서 생성되는 고유 ID",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "사용자의 고유 아이디 (이메일 형식)", example = "test@naver.com")
    private String email;

    @Schema(description = "사용자 이름", example = "고등어#1234")
    private String name;

    @Schema(description = "소셜 로그인 정보")
    private String socialLoginInfo;

    @Schema(description = "사용자 권한", accessMode = Schema.AccessMode.READ_ONLY)
    private String role;

    @Schema(description = "사용자 프로필 이미지")
    private String profileImg;

    @Schema(description = "사용자 상태", example = "ACTIVE / WITHDRAW_REQUEST / DELETED")
    private String status;

    @Schema(description = "영구 삭제까지 남은 일 시 분")
    private WithdrawCountdownDto withdrawCountdown;

    public void setProfileImg(String profileImg) {
        this.profileImg = ImagePathHelper.buildProfileImagePath(profileImg, id);
    }
}
