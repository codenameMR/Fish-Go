package com.fishgo.posts.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fishgo.users.domain.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

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

    @Column(columnDefinition = "TEXT")
    private String hashTag;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(columnDefinition = "TEXT")
    private String img;

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

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lon")
    private Double lon;

    private LocalDateTime createdAt;

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
//    private List<Comment> comments;
//
//    @OneToMany(mappedBy = "posts", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Pinpoint> pinpoints;

}
