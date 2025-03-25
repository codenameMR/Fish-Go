package com.fishgo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 교차 출처 요청에서 쿠키 포함을 허용
        configuration.setAllowCredentials(true);

        // 임시로 모든 Origin(*) 허용 (실서비스에서는 특정 Origin으로 제한 필요)
        configuration.addAllowedOriginPattern("*");

        // HTTP 메서드 허용
        configuration.addAllowedMethod("*");

        // 요청 헤더 허용
        configuration.addAllowedHeader("*");

        // 모든 패턴(/**)에 대해 위 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

