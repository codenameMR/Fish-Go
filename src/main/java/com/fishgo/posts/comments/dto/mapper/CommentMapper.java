package com.fishgo.posts.comments.dto.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.exception.CustomException;
import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentCreateRequestDto;
import com.fishgo.posts.comments.dto.CommentMentionDto;
import com.fishgo.posts.comments.dto.CommentResponseDto;
import com.fishgo.posts.comments.dto.ReplyResponseDto;
import com.fishgo.posts.comments.dto.projection.ParentCommentProjection;
import com.fishgo.users.domain.Users;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Map;


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
    @Mapping(target = "mention", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
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
    @Mapping(target = "mentionedUser", ignore = true)
    CommentResponseDto toResponse(Comment comment);

    @AfterMapping
    default void setMentionedUser(@MappingTarget CommentResponseDto response, Comment comment) {
        response.setMentionedUser(toMentionDto(comment));
    }


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
    @Mapping(target = "mentionedUser", ignore = true)
    ReplyResponseDto toReplyResponse(Comment comment);

    @AfterMapping
    default void setMentionedUserForReply(@MappingTarget ReplyResponseDto response, Comment comment) {
        response.setMentionedUser(toMentionDto(comment));
    }


    @Mapping(target = "id", source = "commentId")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "likeCount", source = "likeCount")
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "profileImg", source = "profileImg")
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "firstReply", ignore = true)
    @Mapping(target = "remainingReplyCount", ignore = true)
    @Mapping(target = "mentionedUser", ignore = true)
    CommentResponseDto projectionToResponse(ParentCommentProjection projection);

    @AfterMapping
    default void setMentionedUser(@MappingTarget CommentResponseDto response, ParentCommentProjection projection) {
        // {"mentionUserId":123,"mentionUserName":"foo"}
        String mentionedUserJson = projection.getMentionedUser();

        if(mentionedUserJson != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Map<String, Object> parsedMention = objectMapper.readValue(
                        mentionedUserJson,
                        new TypeReference<>() {}
                );

                CommentMentionDto dto = new CommentMentionDto();
                dto.setId(Long.parseLong((String) parsedMention.get("mentionUserId")));
                dto.setName(parsedMention.get("mentionUserName").toString());

                response.setMentionedUser(dto);

            } catch (JsonProcessingException e) {
                throw new CustomException(ErrorCode.JSON_PARSE_ERROR.getCode(),
                        "JSON parse error");
            }
        }
    }

    private CommentMentionDto toMentionDto(Comment comment){
        if (comment.getMention() == null
                || comment.getMention().getMentionedUser() == null) {
            return null;
        }
        Users mentionedUser = comment.getMention().getMentionedUser();

        CommentMentionDto dto = new CommentMentionDto();
        dto.setId(mentionedUser.getId());
        dto.setName(mentionedUser.getProfile().getName());

        return dto;
    }
}