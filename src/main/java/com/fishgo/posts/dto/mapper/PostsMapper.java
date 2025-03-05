package com.fishgo.posts.dto.mapper;

import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostsMapper {

    // Posts 엔티티를 PostsDto로 변환
    @Mapping(target = "userId", source = "post.users.id")  // users 객체에서 id를 가져와 userId에 매핑
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "isModify", source = "isModify")
    @Mapping(target = "hashTag", source = "hashTag")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "img", source = "img")
    @Mapping(target = "metaData", source = "metaData")
    @Mapping(target = "reportCount", source = "reportCount")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "viewCount", source = "viewCount")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "fishType", source = "fishType")
    @Mapping(target = "fishSize", source = "fishSize")
    PostsDto toDto(Posts post);

    // PostsDto를 Posts 엔티티로 변환
    @Mapping(target = "users", ignore = true)  // users는 Service에서 설정할 예정이므로 ignore
    @Mapping(target = "createdAt", ignore = true)  // createdAt은 자동으로 처리
    @Mapping(target = "isModify", source = "dto.isModify")
    @Mapping(target = "hashTag", source = "dto.hashTag")
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "contents", source = "dto.contents")
    @Mapping(target = "img", source = "dto.img")
    @Mapping(target = "metaData", source = "dto.metaData")
    @Mapping(target = "reportCount", source = "dto.reportCount")
    @Mapping(target = "isActive", source = "dto.isActive")
    @Mapping(target = "likeCount", source = "dto.likeCount")
    @Mapping(target = "viewCount", source = "dto.viewCount")
    @Mapping(target = "location", source = "dto.location")
    @Mapping(target = "fishType", source = "dto.fishType")
    @Mapping(target = "fishSize", source = "dto.fishSize")
    Posts toEntity(PostsDto dto);
}
