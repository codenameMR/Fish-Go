package com.fishgo.badge.event;

import com.fishgo.badge.dto.BadgeDto.BadgeNotificationDto;
import com.fishgo.badge.service.BadgeService;
import com.fishgo.posts.domain.Posts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 뱃지 이벤트 리스너
 * - 게시글, 댓글 등의 이벤트를 수신하여 뱃지 획득 조건을 확인합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BadgeEventListener {

    private final BadgeService badgeService;

    /**
     * 게시글 작성 이벤트 처리
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        log.debug("Handling post created event: {}", event);
        Posts post = event.post();

        List<BadgeNotificationDto> notifications = badgeService.checkPostRelatedBadges(post);

    }
}