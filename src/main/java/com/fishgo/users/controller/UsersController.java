package com.fishgo.users.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.common.response.AuthResponse;
import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.UsersDto;
import com.fishgo.users.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "사용자 API", description = "회원가입, 로그인 및 인증 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final JwtUtil jwtUtil;
    private final UsersService usersService;

    @Operation(summary = "회원가입", description = "사용자 정보를 기반으로 회원가입을 처리합니다.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UsersDto usersDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            //DTO단에서 검증 오류가 있을 경우 오류 메시지를 추출하여 반환
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());

            ApiResponse<List<String>> errorResponse = new ApiResponse<>(errors, HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            Users user = usersService.registerUser(usersDto);
            ApiResponse<Users> response = new ApiResponse<>("회원가입이 성공적으로 완료되었습니다.", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("회원가입 중 예외 발생 : {}", e.getMessage(), e);
            ApiResponse<List<String>> errorResponse = new ApiResponse<>(e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인을 수행합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsersDto usersDto, HttpServletResponse response){
        Map<String, Object> responseData;

        try {
            // Service에서 반환된 사용자와 Access Token 정보 사용
            responseData = usersService.loginUser(usersDto, response);

        } catch (IllegalArgumentException e) {
            ApiResponse<List<String>> errorResponse = new ApiResponse<>(e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>("로그인 성공 하였습니다.", HttpStatus.OK.value(), responseData);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "리프레시 토큰 갱신", description = "리프레시 토큰을 통해 새로운 Access Token 발급.")
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("refreshToken이 없습니다.", HttpStatus.BAD_REQUEST.value()));
        }

        try {
            String userId = jwtUtil.extractUsername(refreshToken);
            Users user = usersService.findByUserId(userId);

            if (jwtUtil.isTokenValid(refreshToken, user)) {
                log.debug("유저 아이디 >> " + userId + "\n refresh Token >> " + refreshToken);
                String newAccessToken = jwtUtil.generateAccessToken(user);
                return ResponseEntity.ok(new AuthResponse(newAccessToken));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse<>("유효하지 않은 refreshToken", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            log.error("토큰 재발급 중 예외 발생 : {}" + e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>("토큰 갱신 실패", HttpStatus.BAD_REQUEST.value()));
        }
    }
}
