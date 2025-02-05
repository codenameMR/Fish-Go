package com.fishgo.users.controller;

import com.fishgo.users.domain.Users;
import com.fishgo.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request){
        String userId = request.get("userId");
        String name = request.get("name");
        String password = request.get("password");
        String role = request.get("role");

        Users user = usersService.registerUser(userId, name, password, role);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request){
        String userId = request.get("userId");
        String password = request.get("password");

        Users user = usersService.loginUser(userId, password);
        return ResponseEntity.ok(user);
    }
}
