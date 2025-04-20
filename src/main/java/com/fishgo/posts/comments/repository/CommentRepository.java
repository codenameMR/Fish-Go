package com.fishgo.posts.comments.repository;

import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentStatsDto;
import com.fishgo.posts.comments.dto.projection.ParentCommentProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByParentId(Long parentId, Pageable pageable);

    @Query("SELECT NEW com.fishgo.posts.comments.dto.CommentStatsDto(" +
            "COUNT(c), COALESCE(SUM(c.likeCount), 0)) " +
            "FROM Comment c WHERE c.user.id = :userId")
    CommentStatsDto findCommentStatsByUserId(@Param("userId") long userId);

    /**
     * 부모 댓글 + 가장 첫 번째 대댓글 + 남은 대댓글 개수를 한 번에 조회
     */
    @Query(value = """
        WITH RankedReplies AS (
            SELECT
                r.id AS reply_id,
                r.parent_id,
                ROW_NUMBER() OVER (PARTITION BY r.parent_id ORDER BY r.created_at ASC) AS rn,
                (COUNT(*) OVER (PARTITION BY r.parent_id) - 1) AS remaining_count
            FROM comment r
            WHERE r.parent_id IS NOT NULL
        )
        SELECT
            c.id                AS comment_id,
            c.user_id           AS user_id,
            c.contents          AS contents,
            c.created_at        AS created_at,
            c.updated_at        AS updated_at,
            c.like_count        AS like_count,
            p.profile_img       AS profile_img,
            p."name" 	        AS "name",
            rr.reply_id         AS first_reply_id,
            rr.remaining_count  AS remaining_reply_count,
            (
             SELECT json_build_object(
                 'mentionUserId', m.mentioned_user_id,
                 'mentionUserName', pu."name"
               )
             FROM comment_mention m
             JOIN profile pu ON m.mentioned_user_id = pu.user_id
             WHERE m.comment_id = c.id
            ) AS mentioned_user
        FROM comment c
        JOIN profile p
                ON c.user_id = p.user_id
        LEFT JOIN RankedReplies rr
               ON c.id = rr.parent_id
              AND rr.rn = 1
        WHERE c.posts_id = :postId
          AND c.parent_id IS NULL
        ORDER BY c.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM comment c
        WHERE c.posts_id = :postId
          AND c.parent_id IS NULL
        """,
            nativeQuery = true)
    Page<ParentCommentProjection> findParentCommentsWithFirstReply(
            @Param("postId") Long postId,
            Pageable pageable
    );

    Page<Comment> findAllByUser_Id(Long id, Pageable pageable);
}
