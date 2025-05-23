package com.fishgo.posts.dto.mapper;

import com.fishgo.common.constants.UploadPaths;
import com.fishgo.posts.domain.Hashtag;
import com.fishgo.posts.domain.PostImage;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.*;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostsMapper {

    // Posts -> PostResponseDto 변환
    @Mapping(target = "id", source = "posts.id")
    @Mapping(target = "userId", source = "posts.users.id")
    @Mapping(target = "userName", source = "posts.users.profile.name")
    @Mapping(target = "userProfileImg", source = "posts.users.profile.profileImg")
    @Mapping(target = "title", source = "posts.title")
    @Mapping(target = "contents", source = "posts.contents")
    @Mapping(target = "thumbnail", ignore = true)
    @Mapping(target = "likeCount", source = "posts.likeCount")
    @Mapping(target = "viewCount", source = "posts.viewCount")
    @Mapping(target = "createdAt", source = "posts.createdAt")
    PostListResponseDto toPostListResponseDto(Posts posts);

    @AfterMapping // 게시글의 첫번째 이미지를 썸네일로 지정
    default void setThumbnail(Posts posts, @MappingTarget PostListResponseDto dto) {
        dto.setThumbnail(
                posts.getImages().stream()
                        .findFirst()
                        .map(PostImage::getImageName)
                        .orElse(null)
        );
    }

    @Mapping(target = "userId", source = "post.users.id")
    @Mapping(target = "userProfileImg", source = "post.users.profile.profileImg")
    @Mapping(target = "userName", source = "post.users.profile.name")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "hashtag", source = "post.hashtag")  // Set<Hashtag>을 List<String>으로 매핑
    @Mapping(target = "title", source = "title")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "viewCount", source = "viewCount")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "fishType", source = "fishType")
    @Mapping(target = "fishSize", source = "fishSize")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lon", source = "lon")
    @Mapping(target = "liked", ignore = true)
    PostsDto toDtoWithoutImage(Posts post);

    @Mapping(target = "userName", source = "post.users.profile.name")
    @Mapping(target = "userId", source = "post.users.id")
    @Mapping(target = "userProfileImg", source = "post.users.profile.profileImg")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "hashtag", source = "post.hashtag")  // Set<Hashtag>을 List<String>으로 매핑
    @Mapping(target = "title", source = "title")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "images", expression = "java(mapToImageDtoList(post.getImages(), post.getId()))")
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "viewCount", source = "viewCount")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "fishType", source = "fishType")
    @Mapping(target = "fishSize", source = "fishSize")
    @Mapping(target = "lat", source = "lat")
    @Mapping(target = "lon", source = "lon")
    @Mapping(target = "liked", ignore = true)
    PostsDto toDto(Posts post);

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "hashtag", ignore = true)  // List<String>을 Set<Hashtag>로 매핑
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "contents", source = "dto.contents")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "location", source = "dto.location")
    @Mapping(target = "fishType", source = "dto.fishType")
    @Mapping(target = "fishSize", source = "dto.fishSize")
    @Mapping(target = "lat", source = "dto.lat")
    @Mapping(target = "lon", source = "dto.lon")
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
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
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hashtag", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateFromDto(PostsUpdateRequestDto dto, @MappingTarget Posts entity);


    @AfterMapping
    default void linkImages(@MappingTarget Posts post) {
        post.getImages().forEach(img -> img.setPost(post));
    }

    // Set<Hashtag> -> List<String>으로 변환하는 메서드
    default List<String> mapToStringHashtagList(Set<Hashtag> hashtags) {
        if (hashtags == null) return new ArrayList<>();
        return hashtags.stream()
                .map(Hashtag::getName)  // Hashtag의 이름을 String으로 변환
                .collect(Collectors.toList());
    }

    // Set<Image> -> List<ImageDto>으로 변환하는 메서드
    default List<ImageDto> mapToImageDtoList(Set<PostImage> postImages, long postId) {
        if (postImages == null) return new ArrayList<>();
        String postImagePath = UploadPaths.UPLOAD_POSTS.getPath() + postId + "/";

        return postImages.stream()
                .map(postImage -> new ImageDto(postImage.getId(), postImagePath + postImage.getImageName()))
                .collect(Collectors.toList());
    }

}
