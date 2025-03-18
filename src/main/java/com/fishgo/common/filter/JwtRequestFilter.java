package com.fishgo.common.filter;

import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.util.JwtUtil;
import com.fishgo.users.domain.Users;
import com.fishgo.users.repository.UsersRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Component
@AllArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsersRepository usersRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {

            // 1. Access Token 추출
            final Cookie[] cookies = request.getCookies();
            String accessToken = null;
            if(cookies != null){
                Cookie accessTokenCookie = Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals("accessToken"))
                        .findFirst()
                        .orElse(null);
                accessToken = accessTokenCookie != null ? accessTokenCookie.getValue() : null;
            }


            // 2. Access Token 검증
            if (accessToken != null) {

                if (jwtUtil.isAccessBlacklisted(accessToken)) {
                    handleException(response, "이미 로그아웃된 토큰입니다.", ErrorCode.BLACKLISTED_TOKEN.getCode());
                    return;
                }

                long userId = jwtUtil.extractUserId(accessToken);

                if (userId != 0 && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Users user = usersRepository.findById(userId)
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

}