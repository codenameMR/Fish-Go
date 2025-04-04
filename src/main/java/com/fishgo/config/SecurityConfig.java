package com.fishgo.config;

import com.fishgo.common.exception.JwtAuthenticationEntryPoint;
import com.fishgo.common.filter.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable) // 임시 CORS 해제
            .sessionManagement((sessionManagement) ->
                    sessionManagement // 세션 비활성화
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 회원가입, 로그인, 게시글 목록, 게시글 상세, 댓글 목록, 조석 예보, Swagger 관련 요청을 제외한 모든 요청은 권한 필요.
            .authorizeHttpRequests((authorizeRequests) ->
                    authorizeRequests.requestMatchers("/api/auth/register", "/api/auth/login","/swagger-ui/**",
                                    "/api/swagger-ui.html", "/api/v3/api-docs/**", "/api/auth/register","/api/auth/resendVerify","/api/auth/verify").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/{postId}", "/api/comment", "/api/tide/{obsCode}").permitAll()
                            .anyRequest().authenticated()
            )
            // 권한이 없는 요청이 들어왔을 때 동작할 AuthenticationEntryPoint
            .exceptionHandling((exceptionHandling) ->
                    exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
    }


}
