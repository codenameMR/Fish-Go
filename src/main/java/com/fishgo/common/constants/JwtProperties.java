package com.fishgo.common.constants;

import lombok.Getter;

@Getter
public enum JwtProperties {
    WITHDRAW_REQUESTED_USER_ACCESS_TOKEN_EXPIRATION(1000 * 60 * 10),  // 10분
    ACCESS_TOKEN_EXPIRATION(1000 * 60 * 60),  // 임의 1시간
    REFRESH_TOKEN_EXPIRATION(1000 * 60 * 60 * 24 * 15), // 15일
    BLACKLIST_PREFIX_ACCESS("blacklist:access:"),
    BLACKLIST_PREFIX_REFRESH("blacklist:refresh:");

    private final Object value;

    JwtProperties(Object value) {
        this.value = value;
    }

    public int getIntValue() {
        if (value instanceof Integer) {
            return (int) value;
        }
        throw new IllegalArgumentException("Value is not an integer");
    }

}
