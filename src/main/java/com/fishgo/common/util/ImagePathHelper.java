package com.fishgo.common.util;

import com.fishgo.common.constants.UploadPaths;

public class ImagePathHelper {

    public static String buildProfileImagePath(String profileImageName) {
        if(profileImageName == null) return null;

        return UploadPaths.PROFILE.getPath() + profileImageName;
    }

    public static String buildPostImagePath(String postImageName, long postId) {
        if(postImageName == null) return null;

        return UploadPaths.POST.getPath() + postId + "/" + postImageName;
    }
}
