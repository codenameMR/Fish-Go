package com.fishgo.posts.service;

import com.fishgo.posts.domain.Hashtag;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import com.fishgo.posts.dto.mapper.PostsMapper;
import com.fishgo.posts.respository.HashtagRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final UsersRepository usersRepository;
    private final HashtagRepository hashtagRepository;
    private final PostsMapper postsMapper;

    @Transactional
    public PostsDto createPost(PostsDto postsDto, MultipartFile file) {
        Users user = usersRepository.findById(postsDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미지 업로드
        String imgPath = "";
        if (file != null && !file.isEmpty()) {
            imgPath = uploadImage(file, Long.toString(user.getId()));
        }

        // PostsDto에서 Hashtag 처리
//        Set<Hashtag> hashtags = new HashSet<>();
//        if (postsDto.getHashTag() != null) {
//            for (String tag : postsDto.getHashTag()) {
//                Hashtag hashtag = new Hashtag(tag);
//                hashtag.setName(tag);
//                // 먼저 해시태그를 저장
//                Hashtag savedHashtag = hashtagRepository.save(hashtag);
//
//                hashtagRepository.flush();
//
//                hashtags.add(savedHashtag); // 저장된 해시태그 객체를 Set에 추가
//            }
//        }

        // 게시글 저장
        Posts post = postsMapper.toEntity(postsDto);
        post.setUsers(user);
        post.setImg(imgPath);
//      post.setHashTag(hashtags);

        // 해시태그 저장 해결 못함
        // 트랜잭션 때문에 관계 테이블(post_hashtags)에 값을 저장 못하는 것으로 판단됨
        post.setHashTag(null);

        Posts savedPost = postsRepository.save(post);

        return postsMapper.toDto(savedPost);
    }

    public List<PostsDto> searchPosts(String title, String fishType) {
        return postsRepository.searchPosts(title, fishType).stream()
                .map(postsMapper::toDto)
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
            imgPath = uploadImage(file, Long.toString(post.getUsers().getId()));
        }

        post.setTitle(postsDto.getTitle());
        post.setContents(postsDto.getContents());
//        post.setHashTag(postsDto.getHashTag());
        post.setImg(imgPath);
        post.setLat(postsDto.getLat());
        post.setLon(postsDto.getLon());
        post.setLocation(postsDto.getLocation());
        post.setFishType(postsDto.getFishType());
        post.setFishSize(postsDto.getFishSize());
        post.setIsModify(true);

        Posts updatedPost = postsRepository.save(post);

        return postsMapper.toDto(updatedPost);
    }

    @Transactional
    public void deletePost(Long postId) {
        Posts post = findById(postId);

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

    public Posts findById(long postId) {
        return postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    }

    @Transactional
    public PostsDto getPostDetail(Long postId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        // 게시물을 DTO로 변환하여 반환
        return postsMapper.toDto(post);
    }

}
