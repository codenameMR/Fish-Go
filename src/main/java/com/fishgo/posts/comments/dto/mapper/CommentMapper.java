package com.fishgo.posts.comments.dto.mapper;

import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentCreateRequestDto;
import com.fishgo.posts.comments.dto.CommentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface CommentMapper {

    /**
     * 댓글 작성용 DTO -> Entity 변환
     * 실제로 user, post, parent는 Service에서 넣어줄 것이므로 매퍼에서는 ignore합니다.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    // contents만 주입
    @Mapping(target = "contents", source = "dto.contents")
    @Mapping(target = "likeCount", ignore = true)
    Comment toCreateEntity(CommentCreateRequestDto dto);


    /**
     * 댓글 Entity -> 응답용 DTO 변환
     * 응답에 필요한 필드만 매핑합니다.
     */
    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "name", source = "comment.user.name")
    @Mapping(target = "profileImg", source = "comment.user.profileImg")
    @Mapping(target = "contents", source = "comment.contents")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    @Mapping(target = "parentId", source = "comment.parent.id")
    @Mapping(target = "likeCount", source = "comment.likeCount")
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "replies", source = "comment.replies")
    CommentResponseDto toResponse(Comment comment);

    /**
     * Entity 리스트 -> 응답용 DTO 리스트
     */
    List<CommentResponseDto> toResponseList(List<Comment> comments);
}