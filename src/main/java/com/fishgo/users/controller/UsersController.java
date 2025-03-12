package com.fishgo.users.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.ProfileResponseDto;
import com.fishgo.users.dto.UserStatsDto;
import com.fishgo.users.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "사용자 정보 API", description = "프로필, 기록 등 사용자 정보 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UsersService usersService;

    @Operation(summary = "사용자 개요", description = "사용자의 게시글, 댓글에서 받은 좋아요 개수를 반환합니다.")
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<UserStatsDto>> getOverview(@AuthenticationPrincipal Users user) {

        UserStatsDto statsDto = usersService.getUserStats(user);

        return ResponseEntity.ok(new ApiResponse<>("사용자 오버뷰 로드 성공.", HttpStatus.OK.value(), statsDto));
    }

    @Operation(summary = "프로필", description = "사용자의 기본 프로필 정보를 반환합니다.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> getProfile(@AuthenticationPrincipal Users user) {

        ProfileResponseDto profileResponseDto = usersService.getProfile(user);

        return ResponseEntity.ok(new ApiResponse<>("사용자 프로필 로드 성공.", HttpStatus.OK.value(), profileResponseDto));
    }

    @Operation(summary = "프로필 메세지 변경", description = "프로필 메세지를 변경합니다.")
    @PutMapping("/profile-bio")
    public ResponseEntity<ApiResponse<String>> updateProfileMessage(@RequestBody Map<String, String> payload, @AuthenticationPrincipal Users user) {
        usersService.updateBio(user, payload.get("bio"));

        return ResponseEntity.ok(new ApiResponse<>("프로필 메세지 변경 성공", HttpStatus.OK.value()));
    }

    @Operation(summary = "이름 변경", description = "특수문자 불가능, 최소 2글자, 최대 20글자까지 허용")
    @PutMapping("/name")
    public ResponseEntity<ApiResponse<String>> updateProfileName(@RequestBody Map<String, String> payload, @AuthenticationPrincipal Users user) {
        usersService.updateProfileName(user, payload.get("name"));

        return ResponseEntity.ok(new ApiResponse<>("사용자 이름 변경 성공", HttpStatus.OK.value()));
    }

    @Operation(summary = "프로필 이미지 변경", description = "사용자의 프로필 이미지를 변경합니다")
    @PutMapping("/profile-image")
    public ResponseEntity<ApiResponse<String>> updateProfileImage(@RequestParam MultipartFile image, @AuthenticationPrincipal Users user) {
        usersService.updateProfileImg(user, image);

        return ResponseEntity.ok(new ApiResponse<>("프로필 이미지 변경 성공", HttpStatus.OK.value()));
    }
}
