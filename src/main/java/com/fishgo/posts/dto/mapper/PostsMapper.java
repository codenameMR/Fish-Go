package com.fishgo.posts.dto.mapper;

import com.fishgo.posts.domain.Hashtag;
import com.fishgo.posts.domain.Image;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.*;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostsMapper {

    // Posts -> PostResponseDto 변환
    @Mapping(target = "id", source = "posts.id")
    @Mapping(target = "userName", source = "posts.users.name")
    @Mapping(target = "userProfileImg", source = "posts.users.profileImg")
    @Mapping(target = "title", source = "posts.title")
    @Mapping(target = "contents", source = "posts.contents")
    @Mapping(target = "thumbnail", ignore = true)
    @Mapping(target = "likeCount", source = "posts.likeCount")
    @Mapping(target = "viewCount", source = "posts.viewCount")
    @Mapping(target = "createdAt", source = "posts.createdAt")
    PostListResponseDto toResponseDto(Posts posts);

    @AfterMapping // 게시글의 첫번째 이미지를 썸네일로 지정
    default void setThumbnail(Posts posts, @MappingTarget PostListResponseDto dto) {
        dto.setThumbnail(
                posts.getImages().stream()
                        .findFirst()
                        .map(Image::getImgPath)
                        .orElse(null)
        );
    }


    @Mapping(target = "userName", source = "post.users.name")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "isModify", source = "isModify")
    @Mapping(target = "hashtag", source = "post.hashtag")  // Set<Hashtag>을 List<String>으로 매핑
    @Mapping(target = "title", source = "title")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "images", expression = "java(mapToImageDtoList(post.getImages()))")
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "viewCount", source = "viewCount")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "fishType", source = "fishType")
    @Mapping(target = "fishSize", source = "fishSize")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lon", source = "lon")
    PostsDto toDto(Posts post);

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "hashtag", ignore = true)  // List<String>을 Set<Hashtag>로 매핑
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "contents", source = "dto.contents")
    @Mapping(target = "images", source = "dto.images")
    @Mapping(target = "location", source = "dto.location")
    @Mapping(target = "fishType", source = "dto.fishType")
    @Mapping(target = "fishSize", source = "dto.fishSize")
    @Mapping(target = "lat", source = "dto.lat")
    @Mapping(target = "lon", source = "dto.lon")
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isModify", constant = "false")
    Posts toEntity(PostsCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "contents", source = "dto.contents")
    @Mapping(target = "location", source = "dto.location")
    @Mapping(target = "fishType", source = "dto.fishType")
    @Mapping(target = "fishSize", source = "dto.fishSize")
    @Mapping(target = "lat", source = "dto.lat")
    @Mapping(target = "lon", source = "dto.lon")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isModify", constant = "true")
    @Mapping(target = "hashtag", ignore = true)
    void updateFromDto(PostsUpdateRequestDto dto, @MappingTarget Posts entity);


    @AfterMapping
    default void linkImages(@MappingTarget Posts post) {
        post.getImages().forEach(img -> img.setPost(post));
    }


    // List<String> -> Set<Hashtag>으로 변환하는 메서드
    default Set<Hashtag> mapToHashtagSet(List<String> hashtags) {
        if (hashtags == null) return new HashSet<>();
        return hashtags.stream()
                .map(Hashtag::new)  // String을 Hashtag 객체로 변환
                .collect(Collectors.toSet());
    }

    // List<String> -> Set<Iamge>으로 변환하는 메서드
    default Set<Image> mapToImageSet(List<String> images) {
        if (images == null) return new HashSet<>();
        return images.stream()
                .map(Image::new)  // String을 Image 객체로 변환
                .collect(Collectors.toSet());
    }

    // Set<Hashtag> -> List<String>으로 변환하는 메서드
    default List<String> mapToStringHashtagList(Set<Hashtag> hashtags) {
        if (hashtags == null) return new ArrayList<>();
        return hashtags.stream()
                .map(Hashtag::getName)  // Hashtag의 이름을 String으로 변환
                .collect(Collectors.toList());
    }

    // Set<Image> -> List<ImageDto>으로 변환하는 메서드
    default List<ImageDto> mapToImageDtoList(Set<Image> images) {
        if (images == null) return new ArrayList<>();
        return images.stream()
                .map(image -> new ImageDto(image.getId(), image.getImgPath()))
                .collect(Collectors.toList());
    }

}
