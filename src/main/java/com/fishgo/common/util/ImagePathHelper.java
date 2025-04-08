package com.fishgo.common.util;

import com.fishgo.common.constants.UploadPaths;

public class ImagePathHelper {

    public static String buildProfileImagePath(String profileImageName, long userId) {
        if(profileImageName == null) return null;

        return UploadPaths.UPLOAD_PROFILE.getPath() + userId + "/" + profileImageName;
    }

    public static String buildPostImagePath(String postImageName, long postId) {
        if(postImageName == null) return null;

        return UploadPaths.UPLOAD_POSTS.getPath() + postId + "/" + postImageName;
    }
}
