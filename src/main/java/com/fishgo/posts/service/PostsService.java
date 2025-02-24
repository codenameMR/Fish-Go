package com.fishgo.posts.service;

import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Users;
import com.fishgo.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final UsersRepository usersRepository;

    public Posts createPost(PostsDto postsDto, MultipartFile file) {

        Users user = usersRepository.findById(postsDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String imgPath = null;
        if (file != null && !file.isEmpty()) {
            imgPath = uploadImage(file, user.getUserId());
        }

        Posts post = Posts.builder()
                .users(user)
                .hashTag(postsDto.getHashTag())
                .title(postsDto.getTitle())
                .contents(postsDto.getContents())
                .img(imgPath)
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
    public PostsDto updatePost(Long postId, PostsDto postsDto, MultipartFile file) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!post.getUsers().getId().equals(postsDto.getUserId())) {
            throw new IllegalArgumentException("작성자만 수정 가능합니다.");
        }

        String imgPath = post.getImg();
        if (file != null && !file.isEmpty()) {
            if (imgPath != null) {
                deleteOldImage(imgPath);
            }
            imgPath = uploadImage(file, post.getUsers().getUserId());
        }

        post.setTitle(postsDto.getTitle());
        post.setContents(postsDto.getContents());
        post.setHashTag(postsDto.getHashTag());
        post.setImg(imgPath);
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
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        postsRepository.delete(post);
    }

    private String uploadImage(MultipartFile file, String userId) {
        String userDirectory = "uploads/users/" + userId + "/posts/";
        File dir = new File(userDirectory);

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(userDirectory, fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return userDirectory + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    private void deleteOldImage(String imgPath) {
        if (imgPath != null && !imgPath.isEmpty()) {
            Path oldImagePath = Paths.get(imgPath);
            try {
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                throw new RuntimeException("기존 이미지 삭제 실패", e);
            }
        }
    }

}
