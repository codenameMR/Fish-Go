package com.fishgo.posts.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fishgo.users.domain.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PostImage> images = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "post_hashtag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @Builder.Default
    private Set<Hashtag> hashtag = new HashSet<>();

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column
    private String location;

    @Column
    private String fishType;

    @Column(name = "fish_size")
    private Float fishSize;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lon")
    private Double lon;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PostsLike> likes = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.createdAt = LocalDateTime.now();
    }

    @Column(name = "is_modify", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isModify;

//    @OneToMany(mappedBy = "posts", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Pinpoint> pinpoints;

    // 연관관계 메서드
    public void addImage(PostImage postImage) {
        this.images.add(postImage);
        postImage.setPost(this);
    }

    public void removeImage(PostImage postImage) {
        this.images.remove(postImage);
        postImage.setPost(null);
    }

    public void addHashtag(Hashtag hashtag) {
        this.hashtag.add(hashtag);
        hashtag.getPosts().add(this);
    }

    public void removeHashtag(Hashtag hashtag) {
        this.hashtag.remove(hashtag);
        hashtag.getPosts().remove(this);
    }

}
