package com.fishgo.posts.controller;

import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.exception.CustomException;
import com.fishgo.common.response.ApiResponse;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.*;
import com.fishgo.posts.service.PostsLikeService;
import com.fishgo.posts.service.PostsService;
import com.fishgo.users.domain.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileSystemException;
import java.util.List;
import java.util.Set;

@Tag(name = "게시글 API", description = "게시글 생성 및 수정, 검색 기능을 제공하는 API 입니다.")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostsController {

    private final PostsService postsService;
    private final PostsLikeService postsLikeService;

    @Operation(summary = "게시글 목록 조회", description = "page, size로 게시글 목록을 조회 합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostListResponseDto>>> getPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        // 페이지 번호(page), 조회 개수(size)로 PageRequest 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<PostListResponseDto> responseDtos = postsService.getAllPosts(pageable);

        return ResponseEntity.ok(new ApiResponse<>("게시글 조회 성공", HttpStatus.OK.value(), responseDtos));
    }


    @Operation(summary = "게시글 생성", description = "게시글의 내용으로 게시글을 생성 합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PostsDto>> create(@RequestBody PostsCreateRequestDto postsDto,
                                                        @AuthenticationPrincipal Users currentUser) {

            log.info("게시글 생성 요청 받음: {}", postsDto);
            PostsDto savedPost = postsService.createPost(postsDto, currentUser);
            ApiResponse<PostsDto> response = new ApiResponse<>(
                    "게시글이 저장 되었습니다.",
                    HttpStatus.OK.value(),
                    savedPost
            );
            return ResponseEntity.ok(response);
    }

    @Operation(summary = "이미지 업로드 및 삭제", description = "업로드 할 이미지가 있으면 업로드, 삭제 할 이미지가 있으면 삭제 처리 합니다.\n **form-data**로 요청 보내야 합니다.")
    @PutMapping(value = "/{postId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ImageDto>>> uploadImage(@PathVariable(value = "postId") Long postId,
                                                                   @Parameter(description = "업로드 할 이미지 리스트")
                                                                       @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                                                   @Parameter(description = "삭제 할 이미지의 아이디 리스트 ex) [2,4,6]")
                                                                       @RequestPart(value = "deleteImageIds", required = false) Set<Long> deleteImageIds,
                                                                   @AuthenticationPrincipal Users currentUser) throws FileSystemException {

        List<ImageDto> imageList = null;
        // 업로드할 이미지가 있으면 업로드 처리
        if(images != null && !images.isEmpty()){
            imageList = postsService.uploadImages(postId, images, currentUser);
        }

        // 삭제할 이미지가 있으면 삭제 처리
        if(deleteImageIds != null && !deleteImageIds.isEmpty()) {
            imageList = postsService.deleteImages(postId, deleteImageIds, currentUser);
        }

        // 아무 작업도 수행하지 않은경우 400
        if(imageList == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST.getCode(),
                    "업로드 하거나 삭제할 이미지를 지정 해주세요.");
        }

        return ResponseEntity.ok(new ApiResponse<>("이미지 업로드 및 수정에 성공 했습니다.",
                            HttpStatus.OK.value(),
                            imageList));
    }

    @Operation(summary = "게시글 검색", description = "게시글의 제목, 해시태그, 어종 검색 로직 입니다.")
    @GetMapping("/search")
    public List<PostsDto> search (
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String fishType
    ) {
        return postsService.searchPosts(title, fishType);
    }

    @Operation(summary = "게시글 수정", description = "게시글 ID와 내용으로 게시글을 수정합니다.")
    @PutMapping(value = "/{postId}")
    public ResponseEntity<ApiResponse<PostsDto>> updatePost(@PathVariable Long postId,
                                                            @RequestBody PostsUpdateRequestDto postsDto,
                                                            @AuthenticationPrincipal Users currentUser) {


        PostsDto updatedPost = postsService.updatePost(postId, postsDto, currentUser);
        ApiResponse<PostsDto> response = new ApiResponse<>(
                "게시글이 수정 되었습니다.",
                HttpStatus.OK.value(),
                updatedPost
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 삭제", description = "게시글 ID로 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Posts>> deletePost(@PathVariable Long postId, @AuthenticationPrincipal Users currentUser) {
        postsService.deletePost(postId, currentUser);
        ApiResponse<Posts> response = new ApiResponse<>("게시글이 삭제 되었습니다.", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시물 상세 조회", description = "게시물 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostsDto>> getPostDetail(HttpServletRequest request, @PathVariable Long postId, @AuthenticationPrincipal Users currentUser) {
        String redisUserKey = currentUser != null ?
                String.valueOf(currentUser.getId()) : request.getRemoteAddr();

        PostsDto postDetail = postsService.getPostDetail(postId, redisUserKey, currentUser);
        ApiResponse<PostsDto> response = new ApiResponse<>(
                "게시물 조회 성공",
                HttpStatus.OK.value(),
                postDetail
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 좋아요", description = "게시글 ID로 해당 댓글에 좋아요를 누릅니다.")
    @PostMapping("/{postsId}/like")
    public ResponseEntity<ApiResponse<String>> likePosts(@PathVariable Long postsId, @AuthenticationPrincipal Users currentUser) {
        postsLikeService.likePosts(postsId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>("게시글 좋아요 성공", HttpStatus.OK.value()));
    }

    @Operation(summary = "게시글 좋아요 취소", description = "게시글 ID로 해당 댓글에 좋아요를 취소합니다.")
    @DeleteMapping("/{postsId}/like")
    public ResponseEntity<ApiResponse<String>> unlikePosts(@PathVariable Long postsId, @AuthenticationPrincipal Users currentUser) {
        postsLikeService.unlikePosts(postsId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>("게시글 좋아요 취소 성공", HttpStatus.OK.value()));
    }

}
