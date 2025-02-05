package com.fishgo.users.controller;

import com.fishgo.common.response.ApiResponse;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.UsersDto;
import com.fishgo.users.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
    private final UsersService usersService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UsersDto usersDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            //DTO단에서 검증 오류가 있을 경우 오류 메시지를 추출하여 반환
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());

            ApiResponse<List<String>> errorResponse = new ApiResponse<>(String.join(", ", errors), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            Users user = usersService.registerUser(usersDto);
            ApiResponse<Users> response = new ApiResponse<>("회원가입이 성공적으로 완료되었습니다.", HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("회원가입 중 예외 발생 : {}", e.getMessage(), e);
            ApiResponse<String> errorResponse = new ApiResponse<>(e.getMessage(), HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UsersDto usersDto){
        Users user = usersService.loginUser(usersDto);
        return ResponseEntity.ok(user);
    }
}
