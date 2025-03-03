package com.fishgo.common.util;

import com.fishgo.users.domain.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    //512 bit
    private final String originalKey = "qYtjTJEL77ty3e2yCVTDUZ5Bm13czUHO3JkovgseW2Vnzm+l3iBqv2avHUJZt6/8BDPEETy7rk6kplS3zlsTuA==";
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(originalKey.getBytes()); // 안전한 키 생성
    public final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 10; // 10분
    public final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일

    // Access Token 생성
    public String generateAccessToken(Users user) {
        return buildToken(user, ACCESS_TOKEN_EXPIRATION);
    }

    // Refresh Token 생성
    public String generateRefreshToken(Users user) { return buildToken(user, REFRESH_TOKEN_EXPIRATION); }

    private String buildToken(Users users, long expiration) {
        return Jwts.builder()
                .subject(users.getUserId())
                .claim("role", users.getRole())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 토큰에서 사용자 이름 추출
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 토큰 유효성 검사
    public boolean isTokenValid(String token, Users users) {
        final String username = extractUsername(token);
        return (username.equals(users.getUserId()) && !isTokenExpired(token));
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
}
