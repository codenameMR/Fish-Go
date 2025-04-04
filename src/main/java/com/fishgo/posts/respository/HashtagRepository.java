package com.fishgo.posts.respository;

import com.fishgo.posts.domain.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Hashtag findByName(String name);
}
