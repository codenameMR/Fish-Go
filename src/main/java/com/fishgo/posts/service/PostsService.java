package com.fishgo.posts.service;

import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Users;
import com.fishgo.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final UsersRepository usersRepository;

    public Posts createPost(PostsDto postsDto) {

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
                .metaData(postsDto.getMetaData())
                .build();


        return postsRepository.save(post);
    }

    public List<PostsDto> searchPosts(String title, String hashTag, String fishType) {
        List<Posts> posts = postsRepository.searchPosts(title, hashTag, fishType);

        return posts.stream()
                .map(post -> new PostsDto(
                        post.getUsers().getId(),
                        post.getHashTag(),
                        post.getTitle(),
                        post.getContents(),
                        post.getImg(),
                        post.getMetaData(),
                        post.getReportCount(),
                        post.getIsActive(),
                        post.getLikeCount(),
                        post.getViewCount(),
                        post.getLocation(),
                        post.getFishType(),
                        post.getFishSize()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public PostsDto updatePost(Long postId, PostsDto postsDto) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        if (!post.getUsers().getId().equals(postsDto.getUserId())) {
            throw new IllegalArgumentException("작성자만 수정 가능 합니다.");
        }

        post.setTitle(postsDto.getTitle());
        post.setContents(postsDto.getContents());
        post.setHashTag(postsDto.getHashTag());
        post.setImg(postsDto.getImg());
        post.setMetaData(postsDto.getMetaData());
        post.setLocation(postsDto.getLocation());
        post.setFishType(postsDto.getFishType());
        post.setFishSize(postsDto.getFishSize());

        return new PostsDto(
                post.getUsers().getId(),
                post.getHashTag(),
                post.getTitle(),
                post.getContents(),
                post.getImg(),
                post.getMetaData(),
                post.getReportCount(),
                post.getIsActive(),
                post.getLikeCount(),
                post.getViewCount(),
                post.getLocation(),
                post.getFishType(),
                post.getFishSize()
        );
    }

    @Transactional
    public void deletePost(Long postId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        postsRepository.delete(post);
    }

}
