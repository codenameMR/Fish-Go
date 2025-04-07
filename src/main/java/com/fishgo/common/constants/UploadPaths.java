package com.fishgo.common.constants;

import lombok.Getter;

@Getter
public enum UploadPaths {
    ROOT(System.getProperty("user.dir") + "/uploads/"),
    PROFILE_ABSOLUTE(ROOT.getPath() + "profile/"),
    POST_ABSOLUTE(ROOT.getPath() + "posts/"),
    PROFILE_RELATIVE("/uploads/profile/"),
    POST_RELATIVE("/uploads/posts/"),
    TEMP(ROOT.getPath() + "temp/");

    private final String path;

    UploadPaths(String path) {
        this.path = path;
    }

}
