package com.fishgo.badge.service;

import com.fishgo.badge.domain.Badge;
import com.fishgo.badge.domain.BadgeCode;
import com.fishgo.badge.domain.UserBadge;
import com.fishgo.badge.dto.BadgeDto.BadgeCollectionResponseDto;
import com.fishgo.badge.dto.BadgeDto.BadgeNotificationDto;
import com.fishgo.badge.dto.BadgeDto.BadgeResponseDto;
import com.fishgo.badge.dto.mapper.BadgeMapper;
import com.fishgo.badge.repository.BadgeRepository;
import com.fishgo.badge.repository.UserBadgeRepository;
import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.exception.CustomException;
import com.fishgo.posts.comments.repository.CommentRepository;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Users;
import com.fishgo.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UsersRepository usersRepository;
    private final PostsRepository postsRepository;
    private final CommentRepository commentRepository;
    private final BadgeMapper badgeMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BADGE_PROCESSING_KEY = "badge:processing:";
    private static final long PROCESSING_LOCK_TIMEOUT = 10; // 10초

    /**
     * 뱃지 초기화 메서드 - 애플리케이션 시작시 실행
     */
    @Transactional
    public void initializeBadges() {
        // 기존 뱃지가 없을 경우에만 초기화
        if (badgeRepository.count() == 0) {
            log.info("Initializing badges...");

            List<Badge> badgesToSave = Arrays.stream(BadgeCode.values())
                    .map(code -> Badge.builder()
                            .code(code.name())
                            .name(code.getName())
                            .description(code.getDescription())
                            .imageUrl("/images/badges/" + code.name().toLowerCase() + ".png")
                            .achievementCondition(getAchievementCondition(code))
                            .category(code.getCategory().name())
                            .build())
                    .collect(Collectors.toList());

            badgeRepository.saveAll(badgesToSave);
            log.info("Badges initialized. Total: {}", badgesToSave.size());
        }
    }

    private String getAchievementCondition(BadgeCode badgeCode) {
        switch (badgeCode) {
            case FIRST_POST:
                return "첫 물고기를 성공적으로 낚았습니다!";
            case BIG_FISH_CATCHER:
                return "자신의 키만한 대물을 낚았습니다!";
            case DAWN_FISHING:
                return "새벽의 고요한 속에서 낚시에 성공했습니다!";
            case VARIOUS_FISHES:
                return "다양한 종류의 물고기를 수집했습니다!";
            case MARATHON_FISHERMAN:
                return "8시간 이상 연속으로 낚시를 하세요";
            case SEASONAL_FISHERMAN:
                return "봄, 여름, 가을, 겨울 각 계절에 한 번 이상 낚시를 하세요";
            case REGIONAL_EXPLORER:
                return "전국의 낚시터를 탐험하고 있습니다!";
            case FISHING_BLOGGER:
                return "자신의 낚시 이야기를 꾸준히 기록했습니다!";
            case FISHING_NOVICE:
                return "20개 이상의 댓글을 작성하고 30개 이상의 게시물에 참여하세요";
            case FISHING_EXPERT:
                return "50마리 이상의 물고기 잡으세요";
            default:
                return "특별한 조건을 달성하세요!";
        }
    }

    /**
     * 사용자의 모든 뱃지 조회
     */
    @Transactional(readOnly = true)
    public BadgeCollectionResponseDto getUserBadges(Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getCode(), "사용자를 찾을 수 없습니다."));

        // 사용자가 획득한 뱃지들
        List<UserBadge> userBadges = userBadgeRepository.findByUserIdWithBadge(userId);

        // 획득한 뱃지 ID 목록
        Set<Long> achievedBadgeIds = userBadges.stream()
                .map(ub -> ub.getBadge().getId())
                .collect(Collectors.toSet());

        // 모든 뱃지 목록
        List<Badge> allBadges = badgeRepository.findAll();

        // 응답 DTO 변환
        List<BadgeResponseDto> badgeResponseDtoList = allBadges.stream()
                .map(badge -> {
                    BadgeResponseDto dto = badgeMapper.toBadgeResponseDto(badge);

                    // 사용자가 해당 뱃지를 획득했는지 여부 설정
                    if (achievedBadgeIds.contains(badge.getId())) {
                        UserBadge userBadge = userBadges.stream()
                                .filter(ub -> ub.getBadge().getId().equals(badge.getId()))
                                .findFirst()
                                .orElse(null);

                        if (userBadge != null) {
                            dto.setAchieved(true);
                            dto.setAchievedAt(userBadge.getAchievedAt());
                        }
                    } else {
                        dto.setAchieved(false);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        return badgeMapper.toCollectionDto(
                userId,
                user.getProfile().getName(),
                badgeResponseDtoList
        );
    }

    /**
     * 특정 뱃지 획득 처리
     */
    @Transactional
    public BadgeNotificationDto awardBadge(Long userId, String badgeCode) {

        String lockKey = BADGE_PROCESSING_KEY + userId + ":" + badgeCode;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "processing", PROCESSING_LOCK_TIMEOUT, TimeUnit.SECONDS);

        if (locked == null || !locked) {
            log.info("Badge processing already in progress for user {} and badge {}", userId, badgeCode);
            return null;
        }

        try {
            Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getCode(), "사용자를 찾을 수 없습니다."));

            Badge badge = badgeRepository.findByCode(badgeCode)
                    .orElseThrow(() -> new IllegalArgumentException("뱃지를 찾을 수 없습니다: " + badgeCode));

            // 이미 획득한 뱃지인지 확인
            if (userBadgeRepository.existsByUserAndBadge_Code(user, badgeCode)) {
                log.info("User {} already has badge {}", userId, badgeCode);
                return null;
            }

            // 뱃지 획득 처리
            UserBadge userBadge = UserBadge.builder()
                    .user(user)
                    .badge(badge)
                    .build();

            userBadgeRepository.save(userBadge);
            log.info("User {} awarded badge {}", userId, badgeCode);

            // 알림 DTO 반환
            return badgeMapper.toNotificationDto(userBadge);
        } finally {
            // 락 해제
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional
    public List<BadgeNotificationDto> checkPostRelatedBadges(Posts post) {
        List<BadgeNotificationDto> notifications = new ArrayList<>();
        Long userId = post.getUsers().getId();

        // 첫 게시글 작성
        if (postsRepository.countByUsersId(userId) == 1) {
            BadgeNotificationDto notification = awardBadge(userId, BadgeCode.FIRST_POST.name());
            if (notification != null) {
                notifications.add(notification);
            }
        }

        // 큰 물고기
        if (post.getFishSize() != null && post.getFishSize() > 100) {
            BadgeNotificationDto notification = awardBadge(userId, BadgeCode.BIG_FISH_CATCHER.name());
            if (notification != null) {
                notifications.add(notification);
            }
        }

        return notifications;
    }

}