package com.fishgo.users.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.exception.CustomException;
import com.fishgo.common.response.KakaoApiResponse;
import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.JwtRequestDto;
import com.fishgo.users.dto.SignupRequestDto;
import com.fishgo.users.dto.UserResponseDto;
import com.fishgo.users.dto.mapper.UserMapper;
import com.fishgo.users.repository.UsersRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.file.FileSystemException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UsersService usersService;
    private final UsersRepository usersRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    // 카카오 개발자 콘솔에서 발급받은 값
    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    /**
     * 카카오 로그인 비즈니스 로직 처리:
     * 1) 인가 코드 확인
     * 2) 토큰 발급
     * 3) 사용자 정보 확인/등록
     * 4) JWT 토큰 생성 및 쿠키 설정
     */
    public UserResponseDto processKakaoLogin(String authorizationCode,
                                             HttpServletResponse response) throws FileSystemException {

        // 카카오에서 받은 인가코드로 AccessToken 발급
        Map<String, Object> tokenInfo = requestAccessToken(authorizationCode);
        String kakaoAccessToken = (String) tokenInfo.get("access_token");

        // AccessToken으로 사용자 정보 조회
        Object kakaoUserInfoObject = requestUserInfo(kakaoAccessToken);

        // 카카오 API 응답 변환
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoApiResponse kakaoApiResponse = objectMapper.convertValue(kakaoUserInfoObject, KakaoApiResponse.class);
        Map<String, Object> kakaoAccount = kakaoApiResponse.getKakaoAccount();

        // 카카오 프로필의 이메일
        String kakaoEmail = kakaoAccount.get("email").toString();

        // DB 조회
        Users user = usersRepository.findByEmail(kakaoEmail).orElse(null);

        // 최초 가입자 처리
        if(user == null) {
            SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                    .email(kakaoEmail)
                    .password("SNS_USER")
                    .confirmPassword("SNS_USER")
                    .socialInfo("KAKAO")
                    .build();

            user = usersService.registerSocialUser(signupRequestDto);
        }

        // 기존 가입자가 KAKAO가 아닌 다른 소셜/일반 회원이라면 예외 처리
        if(!"KAKAO".equals(user.getSocialLoginInfo())) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED.getCode(), "다른 경로로 가입된 이메일 입니다.");
        }

        // JWT 생성
        JwtRequestDto jwtRequestDto = userMapper.toJwtRequestDto(user);
        String accessToken = jwtUtil.generateAccessToken(jwtRequestDto);
        String refreshToken = jwtUtil.generateRefreshToken(jwtRequestDto);

        // 쿠키에 저장
        response.addCookie(usersService.registerToken("refreshToken", refreshToken));
        response.addCookie(usersService.registerToken("accessToken", accessToken));

        // 최종 메시지
        return userMapper.toUserResponseDto(user);
    }

    /**
     * 인가 코드로 Access Token 요청
     */
    private Map<String, Object> requestAccessToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoRestApiKey);
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        // 카카오 관련 설정 값들
        String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                KAKAO_TOKEN_URL,
                HttpMethod.POST,
                tokenRequest,
                new ParameterizedTypeReference<>() {}
        );

        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            throw new CustomException(ErrorCode.KAKAO_TOKEN_ISSUE_FAIL.getCode(), "카카오 로그인 중 토큰 발급 실패");
        }
        return tokenResponse.getBody();
    }

    /**
     * 받아온 Access Token으로 사용자 정보 요청
     */
    private Map<String, Object> requestUserInfo(String accessToken) {
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("Authorization", "Bearer " + accessToken);

        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);
        String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                userRequest,
                new ParameterizedTypeReference<>() {}
        );

        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            throw new CustomException(ErrorCode.KAKAO_LOGIN_FAILED.getCode(), "사용자 정보 조회 실패");
        }

        return userResponse.getBody();
    }
}