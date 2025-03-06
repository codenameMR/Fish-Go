package com.fishgo.posts.dto.mapper;

import com.fishgo.posts.domain.Hashtag;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostsMapper {

    @Mapping(target = "userId", source = "post.users.id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "isModify", source = "isModify")
    @Mapping(target = "hashTag", source = "post.hashTag")  // Set<Hashtag>을 List<String>으로 매핑
    @Mapping(target = "title", source = "title")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "img", source = "img")
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "viewCount", source = "viewCount")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "fishType", source = "fishType")
    @Mapping(target = "fishSize", source = "fishSize")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lon", source = "lon")
    PostsDto toDto(Posts post);

    // Set<Hashtag> -> List<String>으로 변환하는 메서드
    default List<String> mapToStringList(Set<Hashtag> hashtags) {
        if (hashtags == null) return new ArrayList<>();
        return hashtags.stream()
                .map(Hashtag::getName)  // Hashtag의 이름을 String으로 변환
                .collect(Collectors.toList());
    }

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isModify", source = "dto.isModify", defaultValue = "false")
    @Mapping(target = "hashTag", source = "dto.hashTag")  // List<String>을 Set<Hashtag>로 매핑
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "contents", source = "dto.contents")
    @Mapping(target = "img", source = "dto.img")
    @Mapping(target = "likeCount", source = "dto.likeCount")
    @Mapping(target = "viewCount", source = "dto.viewCount")
    @Mapping(target = "location", source = "dto.location")
    @Mapping(target = "fishType", source = "dto.fishType")
    @Mapping(target = "fishSize", source = "dto.fishSize")
    @Mapping(target = "lat", source = "dto.lat")
    @Mapping(target = "lon", source = "dto.lon")
    Posts toEntity(PostsDto dto);

    // List<String> -> Set<Hashtag>으로 변환하는 메서드
    default Set<Hashtag> mapToHashtagSet(List<String> hashtags) {
        if (hashtags == null) return new HashSet<>();
        return hashtags.stream()
                .map(Hashtag::new)  // String을 Hashtag 객체로 변환
                .collect(Collectors.toSet());
    }
}
