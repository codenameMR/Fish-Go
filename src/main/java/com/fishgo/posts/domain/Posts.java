package com.fishgo.posts.domain;

import com.fishgo.users.domain.Users;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Posts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Column(columnDefinition = "TEXT")
    private String hashTag;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(columnDefinition = "TEXT")
    private String img;

//    @Column(name = "meta_data", columnDefinition = "JSONB")
//    private String metaData;

    @Column(name = "report_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer reportCount;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive;

    @Column(name = "like_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer likeCount;

    @Column(name = "view_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer viewCount;

    @Column(length = 255)
    private String location;

    @Column(length = 255)
    private String fishType;

    @Column(name = "fish_size")
    private Float fishSize;

//    @OneToMany(mappedBy = "posts", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Comment> comments;
//
//    @OneToMany(mappedBy = "posts", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Pinpoint> pinpoints;

}
