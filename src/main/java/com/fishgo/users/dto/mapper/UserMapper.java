package com.fishgo.users.dto.mapper;

import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.JwtRequestDto;
import com.fishgo.users.dto.UserResponseDto;
import com.fishgo.users.dto.WithdrawCountdownDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Duration;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Users -> UserResponseDto
    // Users 엔티티의 필드를 원하는 Dto 필드에 매핑
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "name", source = "profile.name")
    @Mapping(target = "socialLoginInfo", source = "socialLoginInfo")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "profileImg", source = "profile.profileImg")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "withdrawCountdown", ignore = true)
    UserResponseDto toUserResponseDto(Users users);

    @AfterMapping
    default void setRemainingDays(@MappingTarget UserResponseDto dto, Users user) {
        if(user.getWithdrawRequestedAt() == null) return;
        LocalDateTime sevenDaysAfterRequest = user.getWithdrawRequestedAt().plusDays(7);

        // 현재 시간과 7일 뒤의 시간 차이를 구합니다
        Duration duration = Duration.between(LocalDateTime.now(), sevenDaysAfterRequest);

        // 차이를 '일, 시간, 분'으로 계산
        long days = duration.toDays(); // 전체 일 수
        long hours = duration.toHoursPart(); // 일수를 뺀 남은 시간
        long minutes = duration.toMinutesPart(); // 시간을 뺀 남은 분

        // DTO에 남은 시간을 설정
        WithdrawCountdownDto countdown = new WithdrawCountdownDto();
        countdown.setDays(days);
        countdown.setHours(hours);
        countdown.setMinutes(minutes);

        dto.setWithdrawCountdown(countdown);

    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    JwtRequestDto toJwtRequestDto(Users users);

}