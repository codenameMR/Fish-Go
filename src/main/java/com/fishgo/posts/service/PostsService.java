package com.fishgo.posts.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Users;
import com.fishgo.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final UsersRepository usersRepository;

    public Posts createPost(PostsDto postsDto) throws JsonProcessingException {

        Users user = usersRepository.findById(postsDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Posts post = Posts.builder()
                .users(user)
                .hashTag(postsDto.getHashTag())
                .title(postsDto.getTitle())
                .contents(postsDto.getContents())
                .img(postsDto.getImg())
                .reportCount(0)
                .isActive(true)
                .likeCount(0)
                .viewCount(0)
                .location(postsDto.getLocation())
                .fishType(postsDto.getFishType())
                .fishSize(postsDto.getFishSize())
                .build();

        return postsRepository.save(post);
    }

}
