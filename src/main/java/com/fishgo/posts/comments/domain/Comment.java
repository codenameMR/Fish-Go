package com.fishgo.posts.comments.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fishgo.posts.domain.Posts;
import com.fishgo.users.domain.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // USERS 테이블의 PK를 참조하는 기본 키(FK)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // POSTS 테이블의 PK를 참조하는 기본 키(FK)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posts_id", nullable = false)
    private Posts post;

    // 작성 시간
    private LocalDateTime createdAt;

    // 댓글 내용 (TEXT 타입)
    @Column(name = "contents", nullable = false)
    private String contents;

    // 신고 횟수 (기본값 0)
    @Column(name = "report_count", nullable = false)
    private int reportCount;

    // 부모 댓글 (NULL이면 최상위 댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;


    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Comment> replies = new ArrayList<>(); // 대댓글 리스트

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
