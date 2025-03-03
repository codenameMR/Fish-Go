package com.fishgo.users.service;

import com.fishgo.common.util.JwtUtil;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.UsersDto;
import com.fishgo.users.repository.UsersRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostsRepository postsRepository;

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

    public void logoutUser(HttpServletResponse response) {
        // 쿠키 만료시켜서 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
    }

    @Transactional
    public void deleteUser(String refreshToken, HttpServletResponse response) throws Exception {

        String userId = jwtUtil.extractUsername(refreshToken);
        log.debug("userId : {}", userId);
        if(userId == null){
            throw new Exception("사용자 정보를 찾을 수 없습니다.");
        }

        // 쿠키 만료시켜서 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
        usersRepository.deleteByUserId(userId);
    }

    private void createUserDir(String userName) {
        String dir = System.getProperty("user.dir");
        String path = dir + "/uploads/users/" + userName;
        new File(path + "/profile").mkdirs();
        new File(path + "/posts").mkdirs();
    }

    public Users findByUserId(String userId){
        return usersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
    }

    // 사용자의 게시글, 댓글 및 받은 좋아요 총 개수
  /*  public UserOverviewDto getOverview(String userId) {
        Users user = findByUserId(userId);

        // PostsRepository의 단일 쿼리 메서드 호출
        PostStatsDto postStats = postsRepository.findPostStatsByUserId(userId);

        int commentCount = commentRepository.countCommentsByUserId(userId);

        return new UserOverviewDto(
                user.getName(),
                user.getProfileImg(),
                (int) postStats.getPostCount(),
                commentCount,
                (int) postStats.getTotalLikes()
        );
    }*/


}
