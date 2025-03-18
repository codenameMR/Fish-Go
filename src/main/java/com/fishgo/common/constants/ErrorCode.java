package com.fishgo.common.constants;

import lombok.Getter;

@Getter
public enum ErrorCode {
    EXPIRED_TOKEN(401001),
    BLACKLISTED_TOKEN(401002),
    MALFORMED_TOKEN(401003),
    AUTHENTICATION_FAILED(401004),
    INVALID_TOKEN(401005);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }
}
