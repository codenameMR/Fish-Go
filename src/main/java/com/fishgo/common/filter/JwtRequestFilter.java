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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
@Component
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

                // Access Token이 유효한 경우
                if (jwtUtil.isTokenValid(accessToken, user)) {
                    /*UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);*/
                }
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