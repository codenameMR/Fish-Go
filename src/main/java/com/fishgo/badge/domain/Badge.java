package com.fishgo.badge.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "badge")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;  // 뱃지 고유 코드 (예: FIRST_CATCH)

    @Column(nullable = false)
    private String name;  // 뱃지 이름 (예: "첫 낚시왕")

    @Column(nullable = false)
    private String description;  // 뱃지 설명

    @Column(nullable = false)
    private String imageUrl;  // 뱃지 이미지 경로

    @Column(nullable = false)
    private String achievementCondition;  // 달성 조건 설명

    @Column(nullable = false)
    private String category;  // 뱃지 카테고리 (예: POST, COMMENT, FISHING, ACHIEVEMENT)

    @Builder.Default
    @OneToMany(mappedBy = "badge", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserBadge> userBadges = new HashSet<>();
}