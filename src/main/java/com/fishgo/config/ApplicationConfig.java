package com.fishgo.config;

import com.fishgo.badge.service.BadgeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class ApplicationConfig {

    private final BadgeService badgeService;

    @PostConstruct
    public void init() {
        // 애플리케이션 시작시 뱃지 초기화
        badgeService.initializeBadges();
    }
}