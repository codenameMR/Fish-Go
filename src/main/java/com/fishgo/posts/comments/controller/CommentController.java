package com.fishgo.posts.comments.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentDto;
import com.fishgo.posts.comments.service.CommentService;
import com.fishgo.users.domain.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글 관련 API", description = "댓글, 대댓글 관련 CRUD를 수행합니다.")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    // 댓글 조회
    @Operation(summary = "댓글 조회", description = "게시글 ID로 해당 게시물의 댓글을 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getComments(@RequestParam Long postId) {
        List<Comment> comments;

        try {
            comments = commentService.getCommentsByPostId(postId);
        } catch (Exception e) {
            log.error("댓글 조회 실패 : {}", "postId-" + postId + " " + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>("댓글 조회 실패", HttpStatus.BAD_REQUEST.value()));
        }

        return ResponseEntity.ok(new ApiResponse<>("댓글 조회 성공", HttpStatus.OK.value(), comments));
    }

    // 댓글 작성
    @Operation(summary = "댓글 및 대댓글 작성", description = "게시글 ID, 댓글 내용, (대댓글인 경우 부모 댓글의 ID)로 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentDto commentDto) {
        // 현재 로그인 사용자 정보 가져오기
        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        commentDto.setUserId(user.getId());

        CommentDto comment = commentService.saveComment(commentDto);

        return ResponseEntity.ok(new ApiResponse<>("댓글 작성 성공", HttpStatus.OK.value(), comment));
    }

    //댓글 수정
    @Operation(summary = "댓글 수정", description = "댓글 ID와 댓글 내용으로 해당 댓글을 수정합니다.")
    @PatchMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@RequestBody CommentDto commentDto) {
        // 현재 로그인 사용자 정보 가져오기
        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        commentDto.setUserId(user.getId());

        // 서비스 호출
        CommentDto updatedComment;
        try {
            updatedComment = commentService.updateComment(commentDto);
        } catch (Exception e) {
            log.error("댓글 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("댓글 수정 실패", HttpStatus.BAD_REQUEST.value()));
        }
        return ResponseEntity.ok(new ApiResponse<>("댓글 수정 성공", HttpStatus.OK.value(), updatedComment));
    }


    // 댓글 삭제
    @Operation(summary = "댓글 삭제", description = "댓글 ID로 해당 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new ApiResponse<>("댓글 삭제 성공", HttpStatus.OK.value()));
    }


}
