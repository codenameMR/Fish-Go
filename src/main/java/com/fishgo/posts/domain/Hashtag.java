package com.fishgo.posts.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="hashtag")
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "hashtag")
    @Builder.Default
    private Set<Posts> posts = new HashSet<>();

    // @AllArgsConstructor 사용 시, Hashtag(Long id, String name)로 생성자가 생성 되기 때문에 최초 해시태그 생성 시
    // id는 필요 없으므로 name만 받는 생성자 필요
    public Hashtag(String name) {
        this.name = name;
        this.posts = new HashSet<>();
    }

}
