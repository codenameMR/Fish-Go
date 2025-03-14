package com.fishgo.common.filter;

import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.constants.JwtProperties;
import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.domain.Users;
import com.fishgo.users.dto.JwtRequestDto;
import com.fishgo.users.dto.mapper.UserMapper;
import com.fishgo.users.repository.UsersRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


@Component
@AllArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {

            // 1. Access Token 추출
            final String authHeader = request.getHeader("Authorization");
            String accessToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }

            long userId;
            JwtRequestDto jwtRequestDto = null;

            // 2. Access Token 검증
            if (accessToken != null) {

                if (isBlacklisted(accessToken)) {
                    handleException(response, "이미 로그아웃된 토큰입니다.", ErrorCode.BLACKLISTED_TOKEN.getCode());
                    return;
                }

                userId = jwtUtil.extractUserId(accessToken);

                if (userId != 0 && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Users user = usersRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

                    jwtRequestDto = userMapper.toJwtRequestDto(user);

                    // 사용자의 권한을 GrantedAuthority 리스트로 변환 (예: user.getRole() = "ROLE_USER")
                    List<GrantedAuthority> authorities =
                            Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
                    // Authentication 객체 생성
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,   // Principal (user 객체)
                                    null,   // Credentials (비밀번호, 토큰 등) - 이미 인증된 상태이므로 null
                                    authorities // 권한 목록
                            );

                    // SecurityContextHolder에 인증 객체 저장
                    SecurityContextHolder.getContext().setAuthentication(authToken);


                }
            }

           /* // 3. Access Token이 만료된 경우 Refresh Token으로 갱신
            if (accessToken != null && userId != 0 && jwtRequestDto != null && !jwtUtil.isTokenValid(accessToken, jwtRequestDto)) {
                Cookie[] cookies = request.getCookies();
                String refreshToken = null;
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("refreshToken".equals(cookie.getName())) {
                            refreshToken = cookie.getValue();
                            break;
                        }
                    }
                }

                if (refreshToken != null) {
                    // Refresh Token 검증
                    if (jwtUtil.isTokenValid(refreshToken, jwtRequestDto)) {
                        String newAccessToken = jwtUtil.generateAccessToken(jwtRequestDto);
                        response.setHeader("Authorization", "Bearer " + newAccessToken);
                    }
                }
            }*/
        } catch (ExpiredJwtException e) {
            log.error("ExpiredJwtException(만료된 토큰) : {}", e.getMessage());
            handleException(response, "만료된 토큰입니다.", ErrorCode.EXPIRED_TOKEN.getCode());
            return;
        } catch (MalformedJwtException e) {
            log.error("MalformedJwtException(손상된 토큰) : {} ", e.getMessage());
            handleException(response, "손상된 토큰입니다.", ErrorCode.MALFORMED_TOKEN.getCode());
            return;
        }

        chain.doFilter(request, response);
    }

    private void handleException(HttpServletResponse response, String message, int errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String errorResponse = "{\"message\":\"" + message + "\"," +
                                "\"status\":"+ errorCode +"}";
        response.getWriter().write(errorResponse);
    }

    private boolean isBlacklisted(String token) {
        // Redis에 "blacklist:<토큰>" 존재 여부 확인
        return Boolean.TRUE.equals(redisTemplate.hasKey(JwtProperties.BLACKLIST_PREFIX_ACCESS.getValue() + token));
    }

}