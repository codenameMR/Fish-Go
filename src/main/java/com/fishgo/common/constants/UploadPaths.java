package com.fishgo.common.constants;

import lombok.Getter;

@Getter
public enum UploadPaths {
    PROFILE("profile/"),
    POST("posts/"),
    UPLOAD_PROFILE("/uploads/profile/"),
    UPLOAD_POSTS("/uploads/posts/"),
    TEMP("/uploads/temp/");

    private final String path;

    UploadPaths(String path) {
        this.path = path;
    }

}
