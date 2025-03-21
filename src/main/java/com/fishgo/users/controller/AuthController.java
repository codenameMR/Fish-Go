package com.fishgo.users.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.common.response.AuthResponse;
import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.*;
import com.fishgo.users.dto.mapper.UserMapper;
import com.fishgo.users.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "사용자 인증 API", description = "회원가입, 로그인 및 인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UsersService usersService;
    private final UserMapper userMapper;

    @Operation(summary = "회원가입", description = "사용자 정보를 기반으로 회원가입을 처리합니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody @Valid SignupRequestDto usersDto, BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            //DTO단에서 검증 오류가 있을 경우 오류 메시지를 추출하여 반환
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());

            ApiResponse<String> errorResponse = new ApiResponse<>(errors, HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        usersService.registerUser(usersDto);
        ApiResponse<String> response = new ApiResponse<>("회원가입이 성공적으로 완료되었습니다.", HttpStatus.OK.value());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam("email") String email,
            @RequestParam("code") String code) throws Exception {

        boolean isVerified = usersService.verifyEmail(email, code);

        if (isVerified) {
            return ResponseEntity.ok(new ApiResponse<>("이메일 인증이 성공적으로 완료되었습니다.", HttpStatus.OK.value()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("인증 코드가 유효하지 않습니다.", HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인을 수행합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto usersDto, HttpServletResponse response){
        LoginResponseDto loginResponseDto;

        // Service에서 반환된 사용자와 Access Token 정보 사용
        loginResponseDto = usersService.loginUser(usersDto, response);

        ApiResponse<LoginResponseDto> apiResponse = new ApiResponse<>("로그인 성공 하였습니다.", HttpStatus.OK.value(), loginResponseDto);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "로그아웃", description = "FE에서 AccessToken, BE에서는 RefreshToken을 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        usersService.logoutUser(response);

        return ResponseEntity.ok(new ApiResponse<>("로그아웃 성공 하였습니다.", HttpStatus.OK.value()));
    }

    @Operation(summary = "회원탈퇴")
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<String>> delete(HttpServletResponse response,
                                                      @AuthenticationPrincipal Users currentUser) {
        usersService.deleteUser(response, currentUser);

        return ResponseEntity.ok(new ApiResponse<>("회원탈퇴가 성공적으로 완료되었습니다.", HttpStatus.OK.value()));
    }

    @Operation(summary = "리프레시 토큰 갱신", description = "RefreshToken을 통해 새로운 AccessToken 발급.")
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("RefreshToken이 없습니다.", HttpStatus.BAD_REQUEST.value()));
        }

        try {
            long userId = jwtUtil.extractUserId(refreshToken);
            Users user = usersService.findByUserId(userId);
            JwtRequestDto jwtRequestDto = userMapper.toJwtRequestDto(user);

            if (jwtUtil.isTokenValid(refreshToken, jwtRequestDto)) {
                String newAccessToken = jwtUtil.generateAccessToken(jwtRequestDto);
                return ResponseEntity.ok(new AuthResponse(newAccessToken));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse<>("유효하지 않은 refreshToken", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            log.error("토큰 재발급 중 예외 발생 : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>("토큰 갱신 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }
}
