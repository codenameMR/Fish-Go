package com.fishgo.common.filter;

import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.domain.Users;
import com.fishgo.users.repository.UsersRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
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
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. Access Token 추출
        final String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        String username = null;
        Users user = null;

        // 2. Access Token 검증
        if (accessToken != null) {
            username = jwtUtil.extractUsername(accessToken);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                user = usersRepository.findByUserId(username)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

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

        // 3. Access Token이 만료된 경우 Refresh Token으로 갱신
        if (accessToken != null && username != null && user != null && !jwtUtil.isTokenValid(accessToken, user)) {
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
                if (jwtUtil.isTokenValid(refreshToken, user)) {
                    String newAccessToken = jwtUtil.generateAccessToken(user);
                    response.setHeader("Authorization", "Bearer " + newAccessToken);
                }
            }
        }

        chain.doFilter(request, response);
    }
}