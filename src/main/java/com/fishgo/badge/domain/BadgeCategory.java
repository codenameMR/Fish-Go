package com.fishgo.badge.domain;

import lombok.Getter;

@Getter
public enum BadgeCategory {
    POST("게시글"),
    COMMENT("댓글"),
    FISHING("낚시"),
    ACHIEVEMENT("성취");

    private final String displayName;

    BadgeCategory(String displayName) {
        this.displayName = displayName;
    }
}
