package com.fishgo.posts.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import com.fishgo.posts.service.PostsService;
import com.fishgo.users.domain.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "게시글 API", description = "게시글 생성 및 수정, 검색 기능을 제공하는 API 입니다.")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostsController {

    private static final Logger logger = LoggerFactory.getLogger(PostsController.class);
    private final PostsService postsService;

    @Operation(summary = "게시글 생성", description = "게시글의 내용으로 게시글을 생성 합니다.")
    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestPart(value = "param") PostsDto postsDto,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        postsDto.setUserId(user.getId());

        try {
            log.info("게시글 생성 요청 받음: {}", postsDto);
            PostsDto savedPost = postsService.createPost(postsDto, file);
            ApiResponse<PostsDto> response = new ApiResponse<>(
                    "게시글이 저장 되었습니다.",
                    HttpStatus.OK.value(),
                    savedPost
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("게시글 생성 중 예외 발생 : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>("게시글 저장 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "게시글 조회", description = "게시글의 제목, 해시태그, 어종 검색 로직 입니다.")
    @GetMapping("/search")
    public List<PostsDto> search (
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String fishType
    ) {
        return postsService.searchPosts(title, fishType);
    }

    @Operation(summary = "게시글 수정", description = "게시글 ID와 내용으로 게시글을 수정합니다.")
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestPart(value = "param") PostsDto postsDto,
                                        @RequestParam(value = "file", required = false) MultipartFile file) {

        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        postsDto.setUserId(user.getId());

        try {
            PostsDto updatedPost = postsService.updatePost(postId, postsDto, file);
            ApiResponse<PostsDto> response = new ApiResponse<>(
                    "게시글이 수정 되었습니다.",
                    HttpStatus.OK.value(),
                    updatedPost
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("게시글 수정 중 예외 발생 : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>("게시글 수정 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "게시글 삭제", description = "게시글 ID로 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            postsService.deletePost(postId);
            ApiResponse<Posts> response = new ApiResponse<>("게시글이 삭제 되었습니다.", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("게시글 삭제 중 예외 발생 : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>("게시글 삭제 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {
        try {
            PostsDto postDetail = postsService.getPostDetail(postId);
            ApiResponse<PostsDto> response = new ApiResponse<>(
                    "게시물 조회 성공",
                    HttpStatus.OK.value(),
                    postDetail
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("게시물 조회 중 예외 발생 : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>("게시물 조회 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }

}
