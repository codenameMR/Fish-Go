package com.fishgo.users.controller;

import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.service.UsersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 정보 API", description = "프로필, 기록 등 사용자 정보 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UsersService usersService;
    private final JwtUtil jwtUtil;

  /*  @Operation(summary = "사용자 개요", description = "사용자의 게시글, 댓글, 받은 좋아요 개수를 반환합니다.")
    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(@RequestHeader("Authorization") String token) {

        String userId = jwtUtil.extractUsername(token);
        UserOverviewDto overviewDto = usersService.getOverview(userId);

        return ResponseEntity.ok(new ApiResponse<>("사용자 오버뷰 로드 성공.", HttpStatus.OK.value(), overviewDto));
    }*/


}
