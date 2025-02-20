package com.fishgo.posts.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import com.fishgo.posts.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostsController {

    private static final Logger logger = LoggerFactory.getLogger(PostsController.class);
    private final PostsService postsService;

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestPart(value = "param") PostsDto postsDto,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            logger.info("게시글 생성 요청 받음: " + postsDto);
            Posts savedPost = postsService.createPost(postsDto, file);
            ApiResponse<Posts> response = new ApiResponse<>(
                    "게시글이 저장 되었습니다.",
                    HttpStatus.OK.value(),
                    savedPost
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("게시글 생성 중 예외 발생 : {}" + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse("게시글 저장 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/search")
    public List<PostsDto> search (
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String hashTag,
            @RequestParam(required = false) String fishType
    ) {
        return postsService.searchPosts(title, hashTag, fishType);
    }


    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestPart(value = "param") PostsDto postsDto,
                                        @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            PostsDto updatedPost = postsService.updatePost(postId, postsDto, file);
            ApiResponse<PostsDto> response = new ApiResponse<>(
                    "게시글이 수정 되었습니다.",
                    HttpStatus.OK.value(),
                    updatedPost
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("게시글 수정 중 예외 발생 : {}" + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse("게시글 수정 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            postsService.deletePost(postId);
            ApiResponse<Posts> response = new ApiResponse<>("게시글이 삭제 되었습니다.", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("게시글 삭제 중 예외 발생 : {}" + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse("게시글 삭제 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }

}
