package com.fishgo.posts.comments.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.posts.comments.dto.*;
import com.fishgo.posts.comments.service.CommentLikeService;
import com.fishgo.posts.comments.service.CommentService;
import com.fishgo.users.domain.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<Page<CommentResponseDto>>> getComments(@RequestParam Long postId,
                                                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                                                             @RequestParam(value = "size", defaultValue = "20") int size,
                                                                             @AuthenticationPrincipal Users currentUser) {
        // 페이지 번호(page), 조회 개수(size)로 PageRequest 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<CommentResponseDto> commentsDtoList = commentService.getParentCommentsWithFirstReply(postId, pageable, currentUser);

        return ResponseEntity.ok(new ApiResponse<>("댓글 조회 성공", HttpStatus.OK.value(), commentsDtoList));
    }

    @Operation(summary = "대댓글 조회", description = "부모 댓글의 ID로 댓글을 조회합니다. 남은 대댓글 수(remainingRepliesCount)," +
                                        " 대댓글 정보가 담긴 Page 객체를 반환합니다.")
    @GetMapping("/reply")
    public ResponseEntity<ApiResponse<RepliesResponseDto>> getReply(@RequestParam Long commentId,
                                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                                                    @AuthenticationPrincipal Users currentUser) {
        // 페이지 번호(page), 조회 개수(size)로 PageRequest 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        RepliesResponseDto replies = commentService.getReplies(commentId, pageable, currentUser);

        return ResponseEntity.ok(new ApiResponse<>("댓글 조회 성공", HttpStatus.OK.value(), replies));
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
        CommentResponseDto updatedComment = commentService.updateComment(commentDto, currentUser);

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
