package com.fishgo.posts.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "images")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실제 파일 경로나 URL 을 저장
    @Column(name = "img_path", length = 255, nullable = false)
    private String imgPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Posts post;

    public Image(String imgPath) {
        this.imgPath = imgPath;
    }

}