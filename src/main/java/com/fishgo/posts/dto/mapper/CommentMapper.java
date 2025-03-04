package com.fishgo.posts.dto.mapper;


import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "postsId", source = "comment.post.id")
    @Mapping(target = "contents", source = "comment.contents")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    @Mapping(target = "parentId", source = "comment.parent.id")
    CommentDto toDto(Comment comment);

    // 반대로 DTO를 Entity로 변환할 때,
    // user나 post는 ID만 있으면 Entity 전체를 채울 수 없으므로
    // Service에서 user, post 객체를 별도로 찾아서 세팅해주는 방식을 권장합니다.
    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "createdAt", source = "dto.createdAt")
    @Mapping(target = "contents", source = "dto.contents")
    Comment toEntity(CommentDto dto);

    // List 변환 메서드
    List<CommentDto> toDtoList(List<Comment> commentList);
}
