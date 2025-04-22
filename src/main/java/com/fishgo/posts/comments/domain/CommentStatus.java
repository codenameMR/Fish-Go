package com.fishgo.posts.comments.domain;

public enum CommentStatus {
    ACTIVE,         // 정상
    DELETED_BY_USER, // 유저가 삭제
    DELETED_BY_ADMIN, // 관리자 삭제
    USER_WITHDRAWN   // 유저 탈퇴
}