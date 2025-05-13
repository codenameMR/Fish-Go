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

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;
    private final UserMapper userMapper;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        try {

            // 1. Access Token 추출
            String accessToken = jwtUtil.resolveTokenFromCookies(request, "accessToken");


            // 2. Access Token 검증
            if (accessToken != null && jwtUtil.isTokenValid(accessToken)) {

                if (jwtUtil.isAccessBlacklisted(accessToken)) {
                    throw new CustomException(ErrorCode.BLACKLISTED_TOKEN.getCode(), "이미 로그아웃된 토큰입니다.");
                }

                long userId = jwtUtil.extractUserId(accessToken);

                if (userId != 0 && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Users user = usersRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("토큰 발급 중 유저 조회 실패"));

                    setAuthentication(user);
                }
            } else {
                // 액세스 토큰이 유효하지 않은 경우 -> 리프레시 토큰으로 새로 발급 시도
                String refreshToken = jwtUtil.resolveTokenFromCookies(request, "refreshToken");


                if(refreshToken != null && jwtUtil.isTokenValid(refreshToken)) {

                    if (jwtUtil.isRefreshBlacklisted(refreshToken)) {
                        throw new CustomException(ErrorCode.BLACKLISTED_TOKEN.getCode(), "이미 로그아웃된 토큰입니다.");
                    }
                    long userId = jwtUtil.extractUserId(refreshToken);

                    Users user = usersRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("토큰 발급 중 유저 조회 실패"));

                    JwtRequestDto jwtRequestDto = userMapper.toJwtRequestDto(user);
                    String newAccessToken = jwtUtil.generateAccessToken(jwtRequestDto);

                    response.addCookie(jwtUtil.registerToken(newAccessToken, "accessToken"));

                    setAuthentication(user);
                }
            }

        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN.getCode(), "만료된 토큰입니다.");
        } catch (MalformedJwtException e) {
            throw new CustomException(ErrorCode.MALFORMED_TOKEN.getCode(), "손상된 토큰입니다.");
        }

        chain.doFilter(request, response);
    }

    private void setAuthentication(Users user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        // 상태에 따른 추가 권한 부여
        if (UserStatus.WITHDRAW_REQUEST.equals(user.getStatus())) {
            authorities.add(new SimpleGrantedAuthority(UserStatus.WITHDRAW_REQUEST.name()));
        }

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