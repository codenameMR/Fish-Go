package com.fishgo.common.constants;

import lombok.Getter;

@Getter
public enum JwtProperties {
    ACCESS_TOKEN_EXPIRATION(1000 * 60 * 15),  // 15분
    REFRESH_TOKEN_EXPIRATION(1000 * 60 * 60 * 24 * 7), // 7일
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
