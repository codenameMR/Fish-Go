package com.fishgo.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.exception.CustomException;
import com.fishgo.common.response.ApiResponse;
import com.fishgo.common.response.KakaoApiResponse;
import com.fishgo.users.dto.SignupRequestDto;
import com.fishgo.users.service.UsersService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.util.Map;

@Tag(name = "카카오 인증 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/kakao")
public class KakaoAuthController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final UsersService usersService;

    // 카카오 개발자 콘솔에서 발급받은 값
    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @GetMapping
    @Operation(summary = "카카오 로그인", description = "카카오 로그인 페이지로 리다이렉트 합니다.")
    public void redirectToKakao(HttpServletResponse response) throws IOException {
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + kakaoRestApiKey
                + "&redirect_uri=" + encodedRedirectUri;
        response.sendRedirect(kakaoAuthUrl);
    }

    /**
     * 카카오 인증 후 콜백 엔드포인트
     * 인가 코드를 받아 토큰 발급 후 사용자 정보 요청
     * @param authorizationCode 토큰 받기 요청에 필요한 인가 코드
     * @param errorDesc 인증 실패 시 반환되는 에러 메시지
     * @param errorCode 인증 실패 시 반환되는 에러 코드
     * @return ResponseEntity
     */
    @Hidden
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<String>> kakaoCallback(
            @RequestParam(value = "code", required = false) String authorizationCode,
            @RequestParam(value = "error_description", required = false) String errorDesc,
            @RequestParam(value = "error", required = false) String errorCode) throws FileSystemException {

        if (errorCode != null) {
            throw new CustomException(ErrorCode.KAKAO_LOGIN_FAILED.getCode(), "카카오 로그인 실패 : " + errorDesc);
        }
        ObjectMapper objectMapper = new ObjectMapper();

        // 카카오 로그인 성공 후 카카오에서 내려준 인가코드로 액세스토큰 발급
        Map<String, Object> tokenInfo = requestAccessToken(authorizationCode);
        String accessToken = (String) tokenInfo.get("access_token");

        // 발급 받은 액세스 토큰으로 유저 정보 요청
        Object userInfo = requestUserInfo(accessToken);
        KakaoApiResponse apiResponse = objectMapper.convertValue(userInfo, KakaoApiResponse.class);
        Map<String, Object> kakaoUserInfo = apiResponse.getKakaoAccount();

        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                        .email(kakaoUserInfo.get("email").toString())
                        .password("SNS_USER")
                        .confirmPassword("SNS_USER")
                        .socialInfo("KAKAO")
                        .build();

        usersService.registerUser(signupRequestDto);

        ApiResponse<String> response = new ApiResponse<>("카카오 로그인이 성공적으로 완료되었습니다.", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    /**
     * 인가 코드로 Access Token 요청
     * @param authorizationCode 인가 코드
     * @return 토큰 정보가 담긴 Map
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
     * 받아온 accessToken으로 유저 정보 요청
     * @param accessToken 액세스토큰
     * @return 유저 정보를 담은 Map
     */
    private Map<String, Object> requestUserInfo(String accessToken) {
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("Authorization", "Bearer " + accessToken);

        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                        KAKAO_USER_INFO_URL,
                        HttpMethod.GET,
                        userRequest,
                        new ParameterizedTypeReference<>() {}
                );

        return userResponse.getBody();
    }

}

