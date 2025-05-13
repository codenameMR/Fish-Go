package com.fishgo.badge.controller;

import com.fishgo.badge.dto.BadgeDto.BadgeCollectionResponseDto;
import com.fishgo.badge.service.BadgeService;
import com.fishgo.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "뱃지 API", description = "사용자 뱃지 관련 API")
@RestController
@RequestMapping("/badges")
@RequiredArgsConstructor
@Slf4j
public class BadgeController {

    private final BadgeService badgeService;

    @Operation(summary = "특정 사용자의 뱃지 목록 조회", description = "특정 사용자의 뱃지 목록을 조회합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<BadgeCollectionResponseDto>> getUserBadges(
            @Parameter(description = "조회할 사용자 ID", required = true)
            @PathVariable Long userId) {
        BadgeCollectionResponseDto badgeCollection = badgeService.getUserBadges(userId);
        return ResponseEntity.ok(new ApiResponse<>("뱃지 목록 조회 성공", HttpStatus.OK.value(), badgeCollection));
    }
}