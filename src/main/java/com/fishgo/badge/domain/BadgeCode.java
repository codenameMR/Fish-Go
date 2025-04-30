package com.fishgo.badge.domain;

import lombok.Getter;

@Getter
public enum BadgeCode {
    // 게시글 관련 뱃지
    FIRST_POST("첫 낚시왕", "첫 물고기를 성공적으로 낚았습니다!", BadgeCategory.POST),
    BIG_FISH_CATCHER("대물 사냥꾼", "자신의 키만한 대물을 낚았습니다!", BadgeCategory.FISHING),
    DAWN_FISHING("새벽 어부", "새벽의 고요한 속에서 낚시에 성공했습니다!", BadgeCategory.FISHING),
    VARIOUS_FISHES("다양성 챔피언", "다양한 종류의 물고기를 수집했습니다!", BadgeCategory.FISHING),

    // 시간대 관련 뱃지
    MARATHON_FISHERMAN("마라톤 낚시꾼", "지구력의 대가! 장시간 낚시에 성공했습니다!", BadgeCategory.ACHIEVEMENT),
    SEASONAL_FISHERMAN("계절별 낚시왕", "사계절 모두 낚시의 즐거움을 알고 있습니다!", BadgeCategory.ACHIEVEMENT),

    // 활동 관련 뱃지
    REGIONAL_EXPLORER("지역 탐험가", "전국의 낚시터를 탐험하고 있습니다!", BadgeCategory.ACHIEVEMENT),
    FISHING_BLOGGER("낚시 기록가", "자신의 낚시 이야기를 꾸준히 기록했습니다!", BadgeCategory.POST),

    // 전문성 관련 뱃지
    FISHING_NOVICE("소셜 피셔", "낚시 커뮤니티의 활발한 참여자입니다!", BadgeCategory.COMMENT),
    FISHING_EXPERT("전문 어부", "낚시의 달인! 수많은 물고기를 낚았습니다!", BadgeCategory.ACHIEVEMENT);

    private final String name;
    private final String description;
    private final BadgeCategory category;

    BadgeCode(String name, String description, BadgeCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }
}
