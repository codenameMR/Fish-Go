package com.fishgo.users.service;

import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.constants.JwtProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fishgo.common.constants.UploadPaths;
import com.fishgo.common.exception.CustomException;
import com.fishgo.common.service.ImageService;
import com.fishgo.common.util.JwtUtil;
import com.fishgo.common.util.NicknameGenerator;
import com.fishgo.posts.comments.dto.CommentStatsDto;
import com.fishgo.posts.comments.repository.CommentRepository;
import com.fishgo.posts.dto.PostStatsDto;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Profile;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.*;
import com.fishgo.users.dto.mapper.UserMapper;
import com.fishgo.users.repository.ProfileRepository;
import com.fishgo.users.repository.UsersRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.FileSystemException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PostsRepository postsRepository;
    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final ProfileRepository profileRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private final EmailVerService emailVerificationService;
    private final EmailService emailService;

    /**
     * 회원가입 처리 및 프로필 디렉토리 생성
     * @param usersDto 회원가입 요청 객체
     * @throws FileSystemException 프로필 디렉토리 생성 실패시 예외 던지기
     */
    @Transactional
    public void registerUser(SignupRequestDto usersDto) throws FileSystemException, MessagingException, JsonProcessingException {
        if(usersRepository.existsByEmail(usersDto.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // 이메일 인증 코드 생성 및 저장
        String verificationCode = emailVerificationService.generateVerificationCode(usersDto.getEmail());

        // 이메일 전송
        emailService.sendVerificationEmail(usersDto.getEmail(), verificationCode);

        // 사용자 정보를 임시 저장
        emailVerificationService.saveUserInfo(usersDto.getEmail(), usersDto);


    }

    public boolean verifyEmail(String email, String code) throws JsonProcessingException, FileSystemException {
        boolean isVerified = emailVerificationService.verifyCode(email, code);
        if (isVerified) {
            // 임시 저장된 사용자 정보를 가져와서 데이터베이스에 저장
            SignupRequestDto usersDto = emailVerificationService.getUserInfo(email);
            // 초기 랜덤 닉네임 생성
            String randomName = NicknameGenerator.generateNickname();
            boolean isNicknameUsed = usersRepository.existsByProfile_Name(randomName);

            while(isNicknameUsed){
                randomName = NicknameGenerator.generateNickname();
                isNicknameUsed = usersRepository.existsByProfile_Name(randomName);
            }

            // 패스워드 암호화
            String encodedPassword = passwordEncoder.encode(usersDto.getPassword());

            Profile profile = Profile.builder()
                    .name(randomName)
                    .build();

            Users user = Users.builder()
                    .email(usersDto.getEmail())
                    .password(encodedPassword)
                    .role("USER")
                    .build();

            user.addProfile(profile);

            // DB 저장
            Users saveUser = usersRepository.save(user);
            // 프로필 디렉토리 생성
            createUserDir(saveUser.getId());

            // 임시 저장된 사용자 정보 삭제
            emailVerificationService.deleteUserInfo(email);

            // 인증 코드 삭제
            emailVerificationService.deleteVerificationCode(email);

        }
        return isVerified;
    }

    public void resendVerificationCode(String email) throws MessagingException, JsonProcessingException {
        // 이메일 유효성 검사 (간단한 패턴 체크)
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }
        // 인증 코드 재생성
        String newVerificationCode = emailVerificationService.regenerateVerifyCode(email);
        // 이메일 전송
        emailService.sendVerificationEmail(email, newVerificationCode);
    }

    public UserResponseDto loginUser(LoginRequestDto usersDto, HttpServletResponse response) {
        Users user = findByUserEmail(usersDto.getEmail());

        if(!passwordEncoder.matches(usersDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        UserResponseDto userResponseDto = userMapper.toUserResponseDto(user);
        JwtRequestDto jwtRequestDto = userMapper.toJwtRequestDto(user);

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(jwtRequestDto);
        String refreshToken = jwtUtil.generateRefreshToken(jwtRequestDto);

        // Refresh, Access Token을 쿠키에 저장
        response.addCookie(registerToken("refreshToken", refreshToken));
        response.addCookie(registerToken("accessToken", accessToken));

        return userResponseDto;
    }

    public void logoutUser(HttpServletResponse response, String refreshToken, String accessToken) {
        // 쿠키 만료시켜서 삭제
        response.addCookie(invalidateToken("refreshToken"));
        response.addCookie(invalidateToken("accessToken"));

        // AccessToken 블랙리스트 등록
        redisTemplate.opsForValue().set(JwtProperties.BLACKLIST_PREFIX_ACCESS.getValue() + accessToken,
                "true",
                JwtProperties.ACCESS_TOKEN_EXPIRATION.getIntValue() / 1000,
                TimeUnit.SECONDS);

        // RefreshToken 블랙리스트 등록
        redisTemplate.opsForValue().set(JwtProperties.BLACKLIST_PREFIX_REFRESH.getValue() + refreshToken,
                "true",
                JwtProperties.REFRESH_TOKEN_EXPIRATION.getIntValue() / 1000,
                TimeUnit.SECONDS);
    }

    @Transactional
    public void deleteUser(HttpServletResponse response, Users currentUser)  {

        long userId = currentUser.getId();
        log.debug("deleteUser userId : {}", userId);

        // 쿠키 만료시켜서 삭제
        response.addCookie(invalidateToken("refreshToken"));
        response.addCookie(invalidateToken("accessToken"));

        usersRepository.deleteById(userId);
        log.debug("deleteUser successful userId : {}", userId);
    }

    public void refreshToken(HttpServletResponse response, String refreshToken) {
        if(jwtUtil.isRefreshBlacklisted(refreshToken)) {
           throw new CustomException(ErrorCode.BLACKLISTED_TOKEN.getCode(), "블랙리스트 처리된 토큰입니다.");
        }

        long userId = jwtUtil.extractUserId(refreshToken);
        Users user = findByUserId(userId);
        JwtRequestDto jwtRequestDto = userMapper.toJwtRequestDto(user);

        if (jwtUtil.isTokenValid(refreshToken, jwtRequestDto)) {
            String newAccessToken = jwtUtil.generateAccessToken(jwtRequestDto);
            response.addCookie(registerToken("accessToken", newAccessToken));
        } else {
            throw new CustomException(ErrorCode.INVALID_TOKEN.getCode(), "유효하지 않은 토큰입니다.");
        }

    }

    private void createUserDir(long userId) throws FileSystemException {
        String userDirectory = UploadPaths.PROFILE.getPath() + userId + "/";
        boolean wasMkdirSuccessful = new File(userDirectory).mkdirs();

        if(!wasMkdirSuccessful){
            throw new FileSystemException("유저 프로필 디렉토리 생성 실패");
        }
    }

    public Users findByUserId(long userId){
        return usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
    }

    public Users findByUserEmail(String email){
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
    }

    /**
     * 사용자의 게시글, 댓글 및 받은 좋아요 총 개수
     * @param currentUser 현재 로그인한 유저 정보 객체
     * @return 유저 활동 간략 정보 객체
     */
    public UserStatsDto getUserStats(Users currentUser) {
        PostStatsDto postStats = postsRepository.findPostStatsByUserId(currentUser.getId());
        CommentStatsDto commentStats = commentRepository.findCommentStatsByUserId(currentUser.getId());

        long postLikeCount = postStats.getTotalLikes();
        long commentLikeCount = commentStats.getTotalLikes();

        long totalLikeCount = postLikeCount + commentLikeCount;

        return new UserStatsDto(
                currentUser.getProfile().getName(),
                currentUser.getProfile().getProfileImg(),
                postStats.getPostCount(),
                commentStats.getCommentCount() ,
                totalLikeCount
        );
    }

    /**
     * 사용자 기본 프로필 정보 요청
     * @param currentUser 현재 로그인한 유저 정보 객체
     * @return 유저 프로필 응답 객체
     */
    public ProfileResponseDto getProfile(Users currentUser) {

        return new ProfileResponseDto(
                getUserStats(currentUser),
                currentUser.getEmail(),
                currentUser.getProfile().getBio()
        );
    }

    /**
     * 사용자 기본 프로필 정보 요청
     * @param userName 조회 할 유저 이름
     * @return 유저 프로필 응답 객체
     */
    public ProfileResponseDto getProfile(String userName) {
        Users user = usersRepository.findByProfile_Name(userName)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        return new ProfileResponseDto(
                getUserStats(user),
                null,
                user.getProfile().getBio()
        );
    }

    /**
     * 유저 자기소래 업데이트
     * @param currentUser 현재 로그인한 유저 정보 객체
     * @param bio (biography) 자기소개
     */
    public void updateBio(Users currentUser, String bio) {
        if(bio.isBlank()){
            bio = null;
        }
        currentUser.getProfile().setBio(bio);

        usersRepository.save(currentUser);
    }

    /**
     * 유저 프로필 이미지 업데이트
     * @param currentUser 현재 로그인한 유저 정보 객체
     * @param profileImg 변경할 프로필 이미지 파일
     */
    @Transactional
    public void updateProfileImg(Users currentUser, MultipartFile profileImg) {

        if(!imageService.isImageFile(profileImg)){
            throw new IllegalArgumentException("이미지 형식이 아닙니다.");
        }
        String profileImgName = imageService.uploadProfileImage(profileImg, currentUser.getId());

        currentUser.getProfile().setProfileImg(profileImgName);

        usersRepository.save(currentUser);
    }

    /**
     * 유저 이름 업데이트
     * @param currentUser 현재 로그인한 유저 정보 객체
     * @param profileName 변경 할 이름
     */
    public void updateProfileName(Users currentUser, String profileName) {
        if (!profileName.matches("^[a-zA-Z0-9가-힣]{2,10}$")) {
            throw new IllegalArgumentException("닉네임에는 공백이나 특수 문자를 포함할 수 없습니다.");
        }
        if(profileRepository.existsByName(profileName)){
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }

        currentUser.getProfile().setName(profileName);

        usersRepository.save(currentUser);
    }

    /**
     * 토큰 무효화
     * @param token refreshToken or AccessToken
     */
    private Cookie invalidateToken(String token) {

        Cookie cookie = new Cookie(token, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        return cookie;
    }

    /**
     * 토큰 쿠키 생성
     * @param tokenType refreshToken or accessToken
     * @param tokenValue 토큰 값
     */
    private Cookie registerToken(String tokenType, String tokenValue) {
        int maxAge = tokenType.equals("accessToken") ?
                JwtProperties.ACCESS_TOKEN_EXPIRATION.getIntValue() / 1000
                : JwtProperties.REFRESH_TOKEN_EXPIRATION.getIntValue() / 1000;

        Cookie cookie = new Cookie(tokenType, tokenValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 추후 정식 배포 후 true로 변경(https)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        return cookie;
    }

}
