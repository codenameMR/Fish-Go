package com.fishgo.common.filter;

import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.exception.CustomException;
import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.domain.UserStatus;
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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        try {
            // AccessToken 인증 실패시 false return
            if (!processAccessToken(request)) {
                processRefreshToken(request, response);
            }
        } catch (Exception e) {
            handleTokenException(e);
        }
        chain.doFilter(request, response);
    }

    private boolean processAccessToken(HttpServletRequest request) {
        String accessToken = jwtUtil.resolveTokenFromCookies(request, ACCESS_TOKEN);

        if (accessToken != null && jwtUtil.isTokenValid(accessToken)) {

            validateTokenBlacklist(accessToken, true);
            authenticateUser(jwtUtil.extractUserId(accessToken));

            return true;
        }
        return false;
    }

    private void processRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.resolveTokenFromCookies(request, REFRESH_TOKEN);

        if (refreshToken != null && jwtUtil.isTokenValid(refreshToken)) {

            validateTokenBlacklist(refreshToken, false);

            Long userId = jwtUtil.extractUserId(refreshToken);
            Users user = getUserById(userId);
            String newAccessToken = generateNewAccessToken(user);

            response.addCookie(jwtUtil.registerToken(ACCESS_TOKEN, newAccessToken));
            setAuthentication(user);
        }
    }

    private void validateTokenBlacklist(String token, boolean isAccessToken) {
        boolean isBlacklisted = isAccessToken ?
                jwtUtil.isAccessBlacklisted(token) :
                jwtUtil.isRefreshBlacklisted(token);
        if (isBlacklisted) {
            throw new CustomException(ErrorCode.BLACKLISTED_TOKEN.getCode(), "이미 로그아웃된 토큰입니다.");
        }
    }

    private Users getUserById(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("토큰 발급 중 유저 조회 실패"));
    }

    private String generateNewAccessToken(Users user) {
        JwtRequestDto jwtRequestDto = userMapper.toJwtRequestDto(user);
        return jwtUtil.generateAccessToken(jwtRequestDto);
    }

    private void authenticateUser(Long userId) {
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            setAuthentication(getUserById(userId));
        }
    }

    private void handleTokenException(Exception e) {
        if (e instanceof ExpiredJwtException) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN.getCode(), "만료된 토큰입니다.");
        } else if (e instanceof MalformedJwtException) {
            throw new CustomException(ErrorCode.MALFORMED_TOKEN.getCode(), "손상된 토큰입니다.");
        }
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "토큰 처리 중 오류가 발생했습니다.");
    }

    private void setAuthentication(Users user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole()));

        if (UserStatus.WITHDRAW_REQUEST.equals(user.getStatus())) {
            authorities.add(new SimpleGrantedAuthority(UserStatus.WITHDRAW_REQUEST.name()));
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}