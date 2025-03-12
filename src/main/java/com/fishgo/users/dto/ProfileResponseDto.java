package com.fishgo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileResponseDto {

    private UserStatsDto userStats;

    private String email;

    private String bio;

}
