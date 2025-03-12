package com.fishgo.common.constants;

import lombok.Getter;

@Getter
public enum UploadPaths {
    ROOT(System.getProperty("user.dir") + "/uploads/"),
    PROFILE(ROOT.getPath() + "profile/"),
    POST(ROOT.getPath() + "posts/"),
    TEMP(ROOT.getPath() + "temp/");

    private final String path;

    UploadPaths(String path) {
        this.path = path;
    }

}
