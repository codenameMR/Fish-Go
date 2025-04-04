package com.fishgo.posts.service;

import com.fishgo.posts.dto.PinpointDto;
import com.fishgo.posts.respository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final PostsRepository postsRepository;

    public List<PinpointDto> getPinpoints(Double minLat, Double minLon, Double maxLat, Double maxLon, Integer limit) {

        Pageable pageable = PageRequest.of(0, limit);

        return postsRepository.findPostsInRange(minLat, minLon, maxLat, maxLon, pageable).orElse(null);
    }

}
