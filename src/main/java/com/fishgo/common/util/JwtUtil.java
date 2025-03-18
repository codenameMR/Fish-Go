package com.fishgo.common.util;

import com.fishgo.common.constants.JwtProperties;
import com.fishgo.users.dto.JwtRequestDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final RedisTemplate<String, String> redisTemplate;

    //512 bit
    private final String originalKey = "qYtjTJEL77ty3e2yCVTDUZ5Bm13czUHO3JkovgseW2Vnzm+l3iBqv2avHUJZt6/8BDPEETy7rk6kplS3zlsTuA==";
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(originalKey.getBytes()); // 안전한 키 생성


    // Access Token 생성
    public String generateAccessToken(JwtRequestDto dto) {
        return buildToken(dto, JwtProperties.ACCESS_TOKEN_EXPIRATION.getIntValue());
    }

    // Refresh Token 생성
    public String generateRefreshToken(JwtRequestDto dto) { return buildToken(dto, JwtProperties.REFRESH_TOKEN_EXPIRATION.getIntValue()); }

    private String buildToken(JwtRequestDto dto, long expiration) {
        return Jwts.builder()
                .subject(dto.getId().toString())
                .claim("email", dto.getEmail())
                .claim("role", dto.getRole())
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
    public boolean isTokenValid(String token, JwtRequestDto dto) {
        final long userId = extractUserId(token);
        return (userId == dto.getId() && !isTokenExpired(token));
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

    public boolean isAccessBlacklisted(String token) {
        // Redis에 "blacklist:access:<토큰>" 존재 여부 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(JwtProperties.BLACKLIST_PREFIX_ACCESS.getValue() + token));
    }

    public boolean isRefreshBlacklisted(String token) {
        // Redis에 "blacklist:refresh<토큰>" 존재 여부 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(JwtProperties.BLACKLIST_PREFIX_REFRESH.getValue() + token));
    }
}
