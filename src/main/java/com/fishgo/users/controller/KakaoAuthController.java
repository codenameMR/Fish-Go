package com.fishgo.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishgo.users.dto.UserResponseDto;
import com.fishgo.users.service.KakaoAuthService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Tag(name = "카카오 인증 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/kakao")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    // 카카오 개발자 콘솔에서 발급받은 값
    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    @Value("${kakao.front.callback.url}")
    private String frontCallbackUrl;

    @GetMapping
    @Operation(summary = "카카오 로그인", description = "카카오 로그인 페이지로 리다이렉트 합니다. \n " +
            "로그인이 성공 할 경우, 프론트 콜백 경로로 유저 정보가 담긴 userData(Json)가 **Base64로 인코딩 된 채** 리다이렉트 됩니다.  \n" +
            "로그인이 실패 할 경우, 같은 경로로 errorCode와 errorDesc가 리다이렉트 됩니다.")
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
     */
    @Hidden
    @GetMapping("/callback")
    public void kakaoCallback(
            HttpServletResponse response,
            @RequestParam(value = "code", required = false) String authorizationCode,
            @RequestParam(value = "error_description", required = false) String errorDesc,
            @RequestParam(value = "error", required = false) String errorCode
    ) throws IOException {

        if(authorizationCode != null) {
            // 서비스에서 처리한 다음, DTO 반환
            UserResponseDto userResponseDto = kakaoAuthService.processKakaoLogin(authorizationCode, response);

            // DTO to Json 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(userResponseDto);

            // 특수문자처리를 위한 Base64 인코딩
            String encodedData = Base64.getEncoder().encodeToString(jsonData.getBytes());

            response.sendRedirect(frontCallbackUrl + "success?userData=" + encodedData);
        } else {
            log.error("카카오 로그인 실패 - errorCode : {}, errorDesc : {}", errorCode, errorDesc);
            response.sendRedirect(frontCallbackUrl + "error?errorCode=" + errorCode + "&errorDesc=" + errorDesc);
        }

    }


}

