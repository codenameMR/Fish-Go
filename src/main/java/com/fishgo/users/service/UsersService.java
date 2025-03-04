package com.fishgo.users.service;

import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.UsersDto;
import com.fishgo.users.repository.UsersRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import java.io.File;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Users registerUser(UsersDto usersDto){
        if(usersRepository.existsByUserId(usersDto.getUserId())){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(usersDto.getPassword());
        Users user = Users.builder()
                .userId(usersDto.getUserId())
                .name(usersDto.getName())
                .password(encodedPassword)
                .role("USER")
                .build();

        Users saveUser = usersRepository.save(user);

        createUserDir(saveUser.getUserId());

        return saveUser;
    }

    public Map<String, Object> loginUser(UsersDto usersDto, HttpServletResponse response) {
        Users user = findByUserId(usersDto.getUserId());

        if(!passwordEncoder.matches(usersDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 2. 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // 3. Refresh Token을 쿠키에 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS 사용 시
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtUtil.REFRESH_TOKEN_EXPIRATION / 1000));
        response.addCookie(refreshTokenCookie);

        // 사용자 및 Access Token Response 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", user);
        responseData.put("accessToken", accessToken);


        return responseData;
    }

    public Users findByUserId(String userId){
        return usersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
    }


    private void createUserDir(String userName) {
        String dir = System.getProperty("user.dir");
        String path = dir + "/uploads/users/" + userName;
        new File(path + "/profile").mkdirs();
        new File(path + "/posts").mkdirs();
    }
}
