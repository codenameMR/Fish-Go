package com.fishgo.posts.service;

import com.fishgo.posts.domain.Hashtag;
import com.fishgo.posts.domain.Image;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostListResponseDto;
import com.fishgo.posts.dto.PostsCreateRequestDto;
import com.fishgo.posts.dto.PostsDto;
import com.fishgo.posts.dto.PostsUpdateRequestDto;
import com.fishgo.posts.dto.mapper.PostsMapper;
import com.fishgo.posts.respository.HashtagRepository;
import com.fishgo.posts.respository.PostsLikeRepository;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final HashtagRepository hashtagRepository;
    private final PostsMapper postsMapper;
    private final PostsLikeRepository postsLikeRepository;

    public Page<PostListResponseDto> getAllPosts(Pageable pageable) {
        Page<Posts> page = postsRepository.findAll(pageable);

        return page.map(postsMapper::toResponseDto);
    }


    @Transactional
    public PostsDto createPost(PostsCreateRequestDto postsDto, List<MultipartFile> files) {

        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //이미지 업로드 관련 로직처리
        handleImageUpload(postsDto, files, user);

        // Hashtag 처리
        Set<Hashtag> hashtags = processHashtags(postsDto.getHashTag());

        // 3) Posts 엔티티 생성
        Posts newPost = postsMapper.toEntity(postsDto);
        newPost.setUsers(user);
        newPost.setHashtag(hashtags);

        // 4) DB 저장
        postsRepository.save(newPost);

        // 5) 결과 DTO 반환
        return postsMapper.toDto(newPost);

    }

    public List<PostsDto> searchPosts(String title, String fishType) {
        return postsRepository.searchPosts(title, fishType).stream()
                .map(postsMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostsDto updatePost(Long postId, PostsUpdateRequestDto postsDto, List<MultipartFile> newImages) {

        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!post.getUsers().getId().equals(user.getId())) {
            throw new IllegalArgumentException("작성자만 수정 가능합니다.");
        }

        // 기존 이미지
        Set<Image> oldImages = post.getImages();

        // 기존 이미지 중 “유지할 ID 목록”을 제외하고 나머지는 제거
        if (postsDto.getExistingImageIds() != null) {
            Set<Long> keepIds = new HashSet<>(postsDto.getExistingImageIds());
            // 제거 대상 식별
            Set<Image> toRemove = oldImages.stream()
                    .filter(img -> !keepIds.contains(img.getId()))
                    .collect(Collectors.toSet());
            // 제거 처리
            for (Image image : toRemove) {
                post.removeImage(image); // orphanRemoval로 인해 DB에서도 자동 삭제
            }
        }

        // !!추후 실제 파일시스템에서도 삭제처리!!

        // 새로 업로드할 이미지 처리
        if (newImages != null && !newImages.isEmpty()) {
            // ex) 이미지 유효성 체크
            List<MultipartFile> validatedFiles = isImageFile(newImages);
            assert validatedFiles != null;

            for (MultipartFile file : validatedFiles) {
                // 파일 저장 후 Image 생성
                String savedPath = uploadImage(file, String.valueOf(post.getUsers().getId()));

                // 새 Image 엔티티 생성 후 Post에 추가
                Image newImage = new Image();
                newImage.setImgPath(savedPath);
                post.addImage(newImage);  // Post.addImage() 내부에서 newImage.setPost(this)도 호출

            }
        }

        Set<Hashtag> oldHashtags = post.getHashtag();
        Set<Hashtag> newHashtags = processHashtags(postsDto.getHashtag());

        // Post 엔티티의 기존 해시태그 중, newHashtags에 포함되지 않는 것들은 제거
        Set<Hashtag> toRemove = oldHashtags.stream()
                .filter(h -> !newHashtags.contains(h))
                .collect(Collectors.toSet());

        // 연결관계 해제 (다대다이기 때문에 중간 테이블에서 해당 연결만 삭제)
        for (Hashtag h : toRemove) {
            post.removeHashtag(h);
        }

        // 새 해시태그 추가
        Set<Hashtag> toAdd = newHashtags.stream()
                .filter(h -> !oldHashtags.contains(h))
                .collect(Collectors.toSet());

        for (Hashtag h : toAdd) {
            post.addHashtag(h);
        }

        postsMapper.updateFromDto(postsDto, post);
        Posts updatedPost = postsRepository.save(post);

        return postsMapper.toDto(updatedPost);
    }

    @Transactional
    public void deletePost(Long postId) {
        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Posts post = findById(postId);

        if (!post.getUsers().getId().equals(user.getId())) {
            throw new IllegalArgumentException("작성자만 수정 가능합니다.");
        }

        postsLikeRepository.deleteAllByPostsId(postId);
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

    private void handleImageUpload(PostsCreateRequestDto postsDto, List<MultipartFile> files, Users user) {
        // 이미지 검증
        List<MultipartFile> images = isImageFile(files);

        // 이미지 업로드
        List<String> uploadedPaths = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            // 이미지가 10장 넘어가면 예외 처리
            if (images.size() > 10) {
                throw new RuntimeException("최대 10장까지만 업로드 가능합니다.");
            }
            for (MultipartFile image : images) {
                // 업로드 처리
                String savedPath = uploadImage(image, String.valueOf(user.getId()));
                uploadedPaths.add(savedPath);
            }
            // 업로드 결과 저장
            postsDto.setImages(uploadedPaths);
        }
    }

    /**
     * MultipartFile이 실제로 이미지인지 확인한다.
     *
     * @param files 이미지 여부를 확인할 대상 파일
     * @return 이미지이면 매개변수, 아니면 null
     */
    private List<MultipartFile> isImageFile(List<MultipartFile> files) {

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                return null;
            }
            try {
                ImageIO.read(file.getInputStream());
            } catch (IOException e) {
                // 파일을 읽다가 오류가 생기면 이미지가 아닌 것으로 간주
                return null;
            }
        }
        return files;
    }

    private Set<Hashtag> processHashtags(List<String> hashtags) {
        Set<Hashtag> verifiedHashtags = new HashSet<>();

            for (String tag : hashtags) {

                Hashtag existingHashtag = hashtagRepository.findByName(tag);
                if (existingHashtag == null) {

                    existingHashtag = new Hashtag(tag);

                    hashtagRepository.save(existingHashtag);
                }
                verifiedHashtags.add(existingHashtag);
            }
        return verifiedHashtags;
    }



}
