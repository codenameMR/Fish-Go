package com.fishgo.posts.comments.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.common.util.JwtUtil;
import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.service.CommentService;
import com.fishgo.users.domain.Users;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글 관련 API", description = "댓글, 대댓글 관련 요청 API")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    // 댓글 조회
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
    @PostMapping
    public ResponseEntity<?> createComment(
            @RequestParam Long postId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId) {

        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUserId();

        Comment comment = commentService.saveComment(postId, content, userId, parentId);

        return ResponseEntity.ok(new ApiResponse<>("댓글 작성 성공", HttpStatus.OK.value(), comment));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestParam String content
    ) {
        // 현재 로그인 사용자 정보 가져오기
        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = user.getUserId();

        // 서비스 호출
        Comment updatedComment;
        try {
            updatedComment = commentService.updateComment(commentId, content, userId);
        } catch (Exception e) {
            log.error("댓글 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("댓글 수정 실패", HttpStatus.BAD_REQUEST.value()));
        }
        return ResponseEntity.ok(new ApiResponse<>("댓글 수정 성공", HttpStatus.OK.value(), updatedComment));
    }


    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new ApiResponse<>("댓글 삭제 성공", HttpStatus.OK.value()));
    }


}
