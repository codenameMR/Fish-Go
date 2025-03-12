package com.fishgo.users.dto.mapper;

import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.JwtRequestDto;
import com.fishgo.users.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    UserResponseDto toUserResponseDto(Users users);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    JwtRequestDto toJwtRequestDto(Users users);

}