package com.fishgo.common.constants;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(400001),

    EXPIRED_TOKEN(401001),
    BLACKLISTED_TOKEN(401002),
    MALFORMED_TOKEN(401003),
    AUTHENTICATION_FAILED(401004),
    INVALID_TOKEN(401005),

    METHOD_NOT_ALLOWED(404001),
    NOT_FOUND(404002),
    USER_NOT_FOUND(404003),

    REDIS_CONNECTION_FAILURE(500001),
    INTERNAL_SERVER_ERROR(500002);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }
}
