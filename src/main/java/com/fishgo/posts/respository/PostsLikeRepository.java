package com.fishgo.posts.respository;

import com.fishgo.posts.domain.PostsLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostsLikeRepository extends JpaRepository<PostsLike, Long> {

    // 특정 댓글 + 사용자 조합(좋아요 이력) 검색 메서드
    Optional<PostsLike> findByPostsIdAndUserId(Long postId, Long userId);

    // “이미 좋아요가 눌린 상태인지?” 확인할 때 사용
    boolean existsByPostsIdAndUserId(Long postId, Long userId);

    // 게시글 삭제 전 해당 게시글의 좋아요 모두 삭제
    void deleteAllByPostsId(Long postId);
}
