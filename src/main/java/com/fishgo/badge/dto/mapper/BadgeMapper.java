package com.fishgo.badge.dto.mapper;

import com.fishgo.badge.domain.Badge;
import com.fishgo.badge.domain.UserBadge;
import com.fishgo.badge.dto.BadgeDto.BadgeResponseDto;
import com.fishgo.badge.dto.BadgeDto.BadgeCollectionResponseDto;
import com.fishgo.badge.dto.BadgeDto.BadgeNotificationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BadgeMapper {

    BadgeMapper INSTANCE = Mappers.getMapper(BadgeMapper.class);

    @Mapping(target = "achieved", ignore = true)
    @Mapping(target = "achievedAt", ignore = true)
    BadgeResponseDto toBadgeResponseDto(Badge badge);

    List<BadgeResponseDto> toBadgeResponseDtoList(List<Badge> badges);

    @Mapping(target = "badgeCode", source = "badge.code")
    @Mapping(target = "badgeName", source = "badge.name")
    @Mapping(target = "badgeDescription", source = "badge.description")
    @Mapping(target = "badgeImageUrl", source = "badge.imageUrl")
    @Mapping(target = "achievedAt", source = "achievedAt")
    BadgeNotificationDto toNotificationDto(UserBadge userBadge);

    @Named("toResponseDtoWithAchievement")
    default BadgeResponseDto toResponseDtoWithAchievement(UserBadge userBadge) {
        if (userBadge == null) return null;

        return BadgeResponseDto.builder()
                .id(userBadge.getBadge().getId())
                .code(userBadge.getBadge().getCode())
                .name(userBadge.getBadge().getName())
                .description(userBadge.getBadge().getDescription())
                .imageUrl(userBadge.getBadge().getImageUrl())
                .achievementCondition(userBadge.getBadge().getAchievementCondition())
                .category(userBadge.getBadge().getCategory())
                .achieved(true)
                .achievedAt(userBadge.getAchievedAt())
                .build();
    }

    default BadgeCollectionResponseDto toCollectionDto(Long userId, String userName, List<BadgeResponseDto> badges) {
        return BadgeCollectionResponseDto.builder()
                .userId(userId)
                .userName(userName)
                .totalBadges(badges.size())
                .badges(badges)
                .build();
    }
}