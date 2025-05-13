package com.fishgo.common.util;

import com.fishgo.common.constants.JwtProperties;
import com.fishgo.users.domain.UserStatus;
import com.fishgo.users.dto.JwtRequestDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecretKey SECRET_KEY; // 안전한 키 생성

    public JwtUtil(RedisTemplate<String, String> redisTemplate,
                   @Value("${jwt.original-key}") String originalKey) {
        this.redisTemplate = redisTemplate;
        //512 bit
        this.SECRET_KEY = Keys.hmacShaKeyFor(originalKey.getBytes());
    }

    // 탈퇴 신청 유저 Access Token 생성
    public String generateWithdrawRequestedAccessToken(JwtRequestDto dto) {
        return buildToken(dto,
                JwtProperties.WITHDRAW_REQUESTED_USER_ACCESS_TOKEN_EXPIRATION.getIntValue(),
                UserStatus.WITHDRAW_REQUEST);
    }

    // Access Token 생성
    public String generateAccessToken(JwtRequestDto dto) {
        return buildToken(dto,
                JwtProperties.ACCESS_TOKEN_EXPIRATION.getIntValue(),
                UserStatus.ACTIVE);
    }

    // Refresh Token 생성
    public String generateRefreshToken(JwtRequestDto dto) {
        return buildToken(dto,
                JwtProperties.REFRESH_TOKEN_EXPIRATION.getIntValue(),
                UserStatus.ACTIVE);
    }

    private String buildToken(JwtRequestDto dto, long expiration, UserStatus userStatus) {
        return Jwts.builder()
                .subject(dto.getId().toString())
                .claim("email", dto.getEmail())
                .claim("role", dto.getRole())
                .claim("status", userStatus.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 토큰에서 사용자 이름 추출
    public long extractUserId(String token) {
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    // 토큰 유효성 검사
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // 클레임 추출
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 모든 클레임 추출
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 쿠키에서 토큰 읽기
    public String resolveTokenFromCookies(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(cookieName))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * 토큰 무효화
     * @param token refreshToken or AccessToken
     */
    public Cookie invalidateToken(String token) {

        Cookie cookie = new Cookie(token, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        return cookie;
    }

    /**
     * 토큰 쿠키 생성
     * @param tokenType refreshToken or accessToken
     * @param tokenValue 토큰 값
     */
    public Cookie registerToken(String tokenType, String tokenValue) {
        int maxAge = tokenType.equals("accessToken") ?
                JwtProperties.ACCESS_TOKEN_EXPIRATION.getIntValue() / 1000
                : JwtProperties.REFRESH_TOKEN_EXPIRATION.getIntValue() / 1000;

        Cookie cookie = new Cookie(tokenType, tokenValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발서버는 http이기 때문에 임시 false 설정
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        return cookie;
    }

    public void setTokenCookie(UserStatus status, JwtRequestDto jwtRequestDto, HttpServletResponse response) {

        switch (status) {
            // 제한된 토큰 생성
            case WITHDRAW_REQUEST -> {
                String accessToken = generateWithdrawRequestedAccessToken(jwtRequestDto);

                response.addCookie(registerToken("accessToken", accessToken));
            }
            case null, default -> {
                // 토큰 생성
                String accessToken = generateAccessToken(jwtRequestDto);
                String refreshToken = generateRefreshToken(jwtRequestDto);

                // Refresh, Access Token을 쿠키에 저장
                response.addCookie(registerToken("refreshToken", refreshToken));
                response.addCookie(registerToken("accessToken", accessToken));
            }
        }

    }

    public boolean isAccessBlacklisted(String token) {
        // Redis에 "blacklist:access:<토큰>" 존재 여부 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(JwtProperties.BLACKLIST_PREFIX_ACCESS.getValue() + token));
    }

    public boolean isRefreshBlacklisted(String token) {
        // Redis에 "blacklist:refresh<토큰>" 존재 여부 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(JwtProperties.BLACKLIST_PREFIX_REFRESH.getValue() + token));
    }
}
