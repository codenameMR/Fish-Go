package com.fishgo.posts.comments.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDto {
    private Long id;               // 댓글 PK
    private Long userId;           // 작성자 (Users) 식별자
    private Long postsId;           // 대상 게시글 (Posts) 식별자
    private String contents;       // 댓글 내용
    private LocalDateTime createdAt;
    private Long parentId;         // 부모 댓글 ID
    private Long reportCount;
}
