package com.fishgo.posts.service;

import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.domain.PostsLike;
import com.fishgo.posts.respository.PostsLikeRepository;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Users;
import com.fishgo.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostsLikeService {

    private final PostsLikeRepository postsLikeRepository;
    private final PostsRepository postsRepository;
    private final UsersService usersService;

    // 좋아요 누르기
    public void likePosts(Long PostsId) {
        // 현재 로그인 사용자
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 이미 좋아요 눌렀는지 확인 (중복 방지)
        boolean alreadyLiked = postsLikeRepository.existsByPostsIdAndUserId(PostsId, currentUser.getId());
        if (alreadyLiked) {
            throw new IllegalStateException("이미 좋아요를 누른 게시글입니다.");
        }

        // 게시글 조회
        Posts posts = postsRepository.findById(PostsId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 좋아요 이력 생성
        PostsLike postsLike = PostsLike.builder()
                .posts(posts)
                .user(currentUser)
                .build();

        postsLikeRepository.save(postsLike);

        // (선택) Posts의 likeCount 증가
        posts.setLikeCount(posts.getLikeCount() + 1);
    }

    // 좋아요 취소
    public void unlikePosts(Long PostsId) {
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 좋아요 이력 조회
        PostsLike PostsLike = postsLikeRepository
                .findByPostsIdAndUserId(PostsId, currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("좋아요를 누른 기록이 없습니다."));

        // DB에서 제거
        postsLikeRepository.delete(PostsLike);

        // (선택) likeCount 감소
        Posts Posts = PostsLike.getPosts();
        Posts.setLikeCount(Posts.getLikeCount() - 1);
    }
}