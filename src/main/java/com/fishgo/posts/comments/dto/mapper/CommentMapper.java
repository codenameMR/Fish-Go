package com.fishgo.posts.comments.dto.mapper;

import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentCreateRequestDto;
import com.fishgo.posts.comments.dto.CommentResponseDto;
import com.fishgo.posts.comments.dto.ReplyResponseDto;
import com.fishgo.posts.comments.dto.projection.ParentCommentProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


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
    @Mapping(target = "likes", ignore = true)
    Comment toCreateEntity(CommentCreateRequestDto dto);


    /**
     * 댓글 Entity -> 응답용 DTO 변환
     * 응답에 필요한 필드만 매핑합니다.
     */
    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "userId", source = "comment.user.id")
    @Mapping(target = "name", source = "comment.user.profile.name")
    @Mapping(target = "profileImg", source = "comment.user.profile.profileImg")
    @Mapping(target = "contents", source = "comment.contents")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    @Mapping(target = "parentId", source = "comment.parent.id")
    @Mapping(target = "likeCount", source = "comment.likeCount")
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "firstReply", ignore = true)
    @Mapping(target = "remainingReplyCount", ignore = true)
    CommentResponseDto toResponse(Comment comment);

    /**
     * 댓글 Entity -> 응답용 DTO 변환
     * 응답에 필요한 필드만 매핑합니다.
     */
    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "userId", source = "comment.user.id")
    @Mapping(target = "name", source = "comment.user.profile.name")
    @Mapping(target = "profileImg", source = "comment.user.profile.profileImg")
    @Mapping(target = "contents", source = "comment.contents")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    @Mapping(target = "parentId", source = "comment.parent.id")
    @Mapping(target = "likeCount", source = "comment.likeCount")
    @Mapping(target = "liked", ignore = true)
    ReplyResponseDto toReplyResponse(Comment comment);

    @Mapping(target = "id", source = "commentId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "profileImg", source = "profileImg")
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "firstReply", ignore = true)
    @Mapping(target = "remainingReplyCount", ignore = true)
    CommentResponseDto projectionToResponse(ParentCommentProjection projection);

}