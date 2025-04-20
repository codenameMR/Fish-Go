package com.fishgo.users.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.posts.comments.dto.CommentResponseDto;
import com.fishgo.posts.dto.PostListResponseDto;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.*;
import com.fishgo.users.service.UsersService;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "사용자 정보 API", description = "프로필, 기록 등 사용자 정보 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UsersService usersService;

    @Operation(summary = "사용자 개요", description = "사용자의 게시글, 댓글 수 및 받은 좋아요 개수를 반환합니다.")
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<UserStatsDto>> getOverview(@AuthenticationPrincipal Users user) {

        UserStatsDto statsDto = usersService.getUserStats(user);

        return ResponseEntity.ok(new ApiResponse<>("사용자 오버뷰 로드 성공.", HttpStatus.OK.value(), statsDto));
    }

    @Operation(summary = "프로필", description = "userName이 없으면 현재 사용자, 있으면 해당 사용자의 프로필 정보를 불러옵니다.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDto>> getProfile(@AuthenticationPrincipal Users user,
                                                                      @RequestParam(value = "userName", required = false) String userName) {
        ProfileResponseDto profileResponseDto;

        if (userName != null) {
            profileResponseDto = usersService.getProfile(userName);
        } else {
            profileResponseDto = usersService.getProfile(user);
        }

        return ResponseEntity.ok(new ApiResponse<>("사용자 프로필 로드 성공.", HttpStatus.OK.value(), profileResponseDto));
    }

    @Operation(summary = "프로필 메세지 변경", description = "프로필 메세지를 변경합니다.")
    @PutMapping("/profile-bio")
    public ResponseEntity<ApiResponse<String>> updateProfileMessage(@RequestBody UpdateProfileBioRequestDto requestDto,
                                                                    @AuthenticationPrincipal Users user) {
        usersService.updateBio(user, requestDto.getBio());

        return ResponseEntity.ok(new ApiResponse<>("프로필 메세지 변경 성공", HttpStatus.OK.value()));
    }

    @Operation(summary = "이름 변경", description = "특수문자 불가능, 최소 2글자, 최대 20글자까지 허용")
    @PutMapping("/name")
    public ResponseEntity<ApiResponse<String>> updateProfileName(@RequestBody UpdateNameRequestDto requestDto,
                                                                 @AuthenticationPrincipal Users user) {
        usersService.updateProfileName(user, requestDto.getName());

        return ResponseEntity.ok(new ApiResponse<>("사용자 이름 변경 성공", HttpStatus.OK.value()));
    }

    @Operation(summary = "프로필 이미지 변경", description = "사용자의 프로필 이미지를 변경합니다")
    @PutMapping("/profile-image")
    public ResponseEntity<ApiResponse<String>> updateProfileImage(@RequestParam MultipartFile image, @AuthenticationPrincipal Users user) {
        String newImagePath = usersService.updateProfileImg(user, image);

        return ResponseEntity.ok(new ApiResponse<>("프로필 이미지 변경 성공", HttpStatus.OK.value(), newImagePath));
    }

    @Operation(summary = "기록 탭", description = "기록 탭 하단의 최대 크기/무게 및 기타 기록을 반환합니다.")
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<UserRecordsDto>> records(@AuthenticationPrincipal Users currentUser) {

        UserRecordsDto userRecordsDto = usersService.getUserRecords(currentUser);

        return ResponseEntity.ok(new ApiResponse<>("사용자 기록 요청 성공.", HttpStatus.OK.value(),userRecordsDto));
    }

    @Operation(summary = "내 게시글 목록", description = "사용자가 작성한 게시글 목록을 반환합니다.")
    @GetMapping(value = "/posts")
    public ResponseEntity<ApiResponse<Page<PostListResponseDto>>> getMyPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal Users currentUser) {

        // 페이지 번호(page), 조회 개수(size)로 PageRequest 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<PostListResponseDto> responses = usersService.getMyPosts(pageable, currentUser);

        return ResponseEntity.ok(new ApiResponse<>("내 게시글 목록 조회 성공", HttpStatus.OK.value(), responses));
    }

    @Operation(summary = "내 댓글 목록", description = "사용자가 작성한 댓글 목록을 반환합니다.")
    @GetMapping(value = "/comments")
    public ResponseEntity<ApiResponse<Page<CommentResponseDto>>> getMyComments(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal Users currentUser) {

        // 페이지 번호(page), 조회 개수(size)로 PageRequest 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<CommentResponseDto> responses = usersService.getMyComments(pageable, currentUser);

        return ResponseEntity.ok(new ApiResponse<>("내 댓글 목록 조회 성공", HttpStatus.OK.value(), responses));
    }
}
