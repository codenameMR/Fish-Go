package com.fishgo.posts.comments.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.posts.comments.dto.CommentCreateRequestDto;
import com.fishgo.posts.comments.dto.CommentResponseDto;
import com.fishgo.posts.comments.dto.CommentUpdateRequestDto;
import com.fishgo.posts.comments.service.CommentLikeService;
import com.fishgo.posts.comments.service.CommentService;
import com.fishgo.users.domain.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글 관련 API", description = "댓글, 대댓글 관련 CRUD를 수행합니다.")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    @Operation(summary = "댓글 조회", description = "게시글 ID로 해당 게시물의 댓글을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getComments(@RequestParam Long postId, @AuthenticationPrincipal Users currentUser) {
        List<CommentResponseDto> commentsDtoList;

        try {
            commentsDtoList = commentService.getCommentsByPostId(postId, currentUser);
        } catch (Exception e) {
            log.error("댓글 조회 실패 : {}", "postId-" + postId + " " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>("댓글 조회 실패", HttpStatus.BAD_REQUEST.value()));
        }

        return ResponseEntity.ok(new ApiResponse<>("댓글 조회 성공", HttpStatus.OK.value(), commentsDtoList));
    }

    @Operation(summary = "댓글 및 대댓글 작성", description = "게시글 ID, 댓글 내용, (대댓글인 경우 부모 댓글의 ID)로 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(@RequestBody CommentCreateRequestDto commentDto, @AuthenticationPrincipal Users currentUser) {

        CommentResponseDto comment = commentService.saveComment(commentDto, currentUser);

        return ResponseEntity.ok(new ApiResponse<>("댓글 작성 성공", HttpStatus.OK.value(), comment));
    }

    @Operation(summary = "댓글 수정", description = "댓글 ID와 댓글 내용으로 해당 댓글을 수정합니다.")
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(@PathVariable long commentId,
                                                                         @RequestBody CommentUpdateRequestDto commentDto,
                                                                         @AuthenticationPrincipal Users currentUser) {

        commentDto.setCommentId(commentId);
        CommentResponseDto updatedComment;
        try {
            updatedComment = commentService.updateComment(commentDto, currentUser);
        } catch (Exception e) {
            log.error("댓글 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("댓글 수정 실패", HttpStatus.BAD_REQUEST.value()));
        }
        return ResponseEntity.ok(new ApiResponse<>("댓글 수정 성공", HttpStatus.OK.value(), updatedComment));
    }

    @Operation(summary = "댓글 삭제", description = "댓글 ID로 해당 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal Users currentUser) {
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>("댓글 삭제 성공", HttpStatus.OK.value()));
    }

    @Operation(summary = "댓글 좋아요", description = "댓글 ID로 해당 댓글에 좋아요를 누릅니다.")
    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<String>> likeComment(@PathVariable Long commentId, @AuthenticationPrincipal Users currentUser) {
        commentLikeService.likeComment(commentId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>("좋아요 성공", HttpStatus.OK.value()));
    }

    @Operation(summary = "댓글 좋아요 취소", description = "댓글 ID로 해당 댓글에 좋아요를 취소합니다.")
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<String>> unlikeComment(@PathVariable Long commentId, @AuthenticationPrincipal Users currentUser) {
        commentLikeService.unlikeComment(commentId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>("좋아요 취소 성공", HttpStatus.OK.value()));
    }


}
