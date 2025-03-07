package com.fishgo.users.service;

import com.fishgo.common.util.JwtUtil;
import com.fishgo.common.util.NicknameGenerator;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.*;
import com.fishgo.users.dto.mapper.UserMapper;
import com.fishgo.users.repository.UsersRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public void registerUser(SignupRequestDto usersDto) throws Exception{
        if(usersRepository.existsByEmail(usersDto.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String randomName = NicknameGenerator.generateNickname();
        boolean isNicknameUsed = usersRepository.existsByName(randomName);

        while(isNicknameUsed){
            randomName = NicknameGenerator.generateNickname();
            isNicknameUsed = usersRepository.existsByName(randomName);
        }

        String encodedPassword = passwordEncoder.encode(usersDto.getPassword());

        Users user = Users.builder()
                .email(usersDto.getEmail())
                .name(randomName)
                .password(encodedPassword)
                .role("USER")
                .build();

        Users saveUser = usersRepository.save(user);

        createUserDir(saveUser.getEmail());

    }

    public LoginResponseDto loginUser(LoginRequestDto usersDto, HttpServletResponse response) {
        Users user = findByUserEmail(usersDto.getEmail());

        if(!passwordEncoder.matches(usersDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        UserResponseDto userResponseDto = userMapper.toUserResponseDto(user);
        JwtRequestDto jwtRequestDto = userMapper.toJwtRequestDto(user);

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(jwtRequestDto);
        String refreshToken = jwtUtil.generateRefreshToken(jwtRequestDto);

        // Refresh Token을 쿠키에 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS 사용 시
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtUtil.REFRESH_TOKEN_EXPIRATION / 1000));
        response.addCookie(refreshTokenCookie);

        // 사용자 및 Access Token Response 데이터 구성
        LoginResponseDto responseData = new LoginResponseDto();
        responseData.setUser(userResponseDto);
        responseData.setAccessToken(accessToken);


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

        long userId = jwtUtil.extractUserId(refreshToken);
        log.debug("deleteUser userId : {}", userId);
        if(userId == 0){
            throw new Exception("사용자 정보를 찾을 수 없습니다.");
        }

        // 쿠키 만료시켜서 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
        usersRepository.deleteById(userId);
    }

    private void createUserDir(String userName) {
        String dir = System.getProperty("user.dir");
        String path = dir + "/uploads/users/" + userName;
        new File(path + "/profile").mkdirs();
        new File(path + "/posts").mkdirs();
    }

    public Users findByUserId(long userId){
        return usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
    }

    public Users findByUserEmail(String email){
        return usersRepository.findByEmail(email)
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
