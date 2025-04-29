package com.fishgo.badge.controller;

import com.fishgo.badge.dto.BadgeDto.BadgeCollectionResponseDto;
import com.fishgo.badge.service.BadgeService;
import com.fishgo.common.response.ApiResponse;
import com.fishgo.users.domain.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "뱃지 API", description = "사용자 뱃지 관련 API")
@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
@Slf4j
public class BadgeController {

    private final BadgeService badgeService;

    @Operation(summary = "내 뱃지 목록 조회", description = "로그인한 사용자의 뱃지 목록을 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<BadgeCollectionResponseDto>> getMyBadges(@AuthenticationPrincipal Users currentUser) {
        BadgeCollectionResponseDto badgeCollection = badgeService.getUserBadges(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>("뱃지 목록 조회 성공", HttpStatus.OK.value(), badgeCollection));
    }

    @Operation(summary = "특정 사용자의 뱃지 목록 조회", description = "특정 사용자의 뱃지 목록을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<BadgeCollectionResponseDto>> getUserBadges(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        BadgeCollectionResponseDto badgeCollection = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(new ApiResponse<>("뱃지 목록 조회 성공", HttpStatus.OK.value(), badgeCollection));
    }
}