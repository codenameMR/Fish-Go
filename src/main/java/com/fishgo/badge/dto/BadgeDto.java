package com.fishgo.badge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class BadgeDto {

    @Getter
    @Setter
    @Builder
    @Schema(description = "뱃지 응답 DTO")
    public static class BadgeResponseDto {
        private Long id;
        private String code;
        private String name;
        private String description;
        private String imageUrl;
        private String achievementCondition;
        private String category;
        private boolean achieved;
        private LocalDateTime achievedAt;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Schema(description = "뱃지 획득 요청 DTO")
    public static class BadgeAchievementRequestDto {
        private String badgeCode;
        private Long userId;
    }

    @Getter
    @Setter
    @Builder
    @Schema(description = "사용자의 뱃지 컬렉션 응답 DTO")
    public static class BadgeCollectionResponseDto {
        private Long userId;
        private String userName;
        private int totalBadges;
        private List<BadgeResponseDto> badges;
    }

    @Getter
    @Setter
    @Builder
    @Schema(description = "뱃지 획득 알림 DTO")
    public static class BadgeNotificationDto {
        private String badgeCode;
        private String badgeName;
        private String badgeDescription;
        private String badgeImageUrl;
        private LocalDateTime achievedAt;
    }
}