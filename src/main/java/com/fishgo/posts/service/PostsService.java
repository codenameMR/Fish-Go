package com.fishgo.posts.service;

import com.fishgo.common.constants.ErrorCode;
import com.fishgo.common.constants.UploadPaths;
import com.fishgo.common.exception.CustomException;
import com.fishgo.common.service.ImageService;
import com.fishgo.common.service.RedisService;
import com.fishgo.posts.domain.Hashtag;
import com.fishgo.posts.domain.PostImage;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.*;
import com.fishgo.posts.dto.mapper.PostsMapper;
import com.fishgo.posts.respository.HashtagRepository;
import com.fishgo.posts.respository.PostsLikeRepository;
import com.fishgo.posts.respository.PostsRepository;
import com.fishgo.users.domain.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileSystemException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final HashtagRepository hashtagRepository;
    private final PostsMapper postsMapper;
    private final PostsLikeRepository postsLikeRepository;
    private final ImageService imageService;
    private final RedisService redisService;

    /**
     * 게시글 목록
     * @param pageable page, size, 정렬기준을 담은 pageable 객체
     * @return 게시글 목록 응답 DTO
     */
    public Page<PostListResponseDto> getAllPosts(Pageable pageable) {
        Page<Posts> page = postsRepository.findAll(pageable);

        return page.map(postsMapper::toResponseDto);
    }


    /**
     * 게시글 생성
     * @param postsDto 게시글 생성 요청 객체
     * @param user 현재 로그인한 유저 정보 객체
     * @return PostsDTO 객체
     */
    @Transactional
    public PostsDto createPost(PostsCreateRequestDto postsDto, Users user) {

        // Hashtag 처리
        Set<Hashtag> hashtags = processHashtags(postsDto.getHashTag());

        // 3) Posts 엔티티 생성
        Posts newPost = postsMapper.toEntity(postsDto);
        newPost.setUsers(user);
        newPost.setHashtag(hashtags);

        // 4) DB 저장
        postsRepository.save(newPost);

        // 5) 결과 DTO 반환
        return postsMapper.toDtoWithoutImage(newPost);

    }

    @Transactional
    public List<ImageDto> uploadImages(Long postId, List<MultipartFile> images, Users currentUser) throws FileSystemException{
        Posts post = findById(postId);

        if(!Objects.equals(post.getUsers().getId(), currentUser.getId())){
            throw new CustomException(ErrorCode.UNAUTHORIZED.getCode(), "다른 유저의 게시글에 업로드 할 수 없습니다.");
        }

        // 이미지 서버 및 DB에 저장
        if(imageService.isImageFile(images)) {

            // 이미지가 10장 넘어가면 예외 처리
            if (images.size() > 10) {
                throw new RuntimeException("최대 10장까지만 업로드 가능합니다.");
            }
            for (MultipartFile image : images) {
                // 업로드 처리
                String savedFileName = imageService.uploadPostImage(image, post.getId());

                // DB 저장
                post.addImage(new PostImage(savedFileName));
            }
        }

        return postsMapper.toDto(post).getImages();
    }

    @Transactional
    public List<ImageDto> deleteImages(Long postId, Set<Long> deleteImageIds, Users currentUser) {
        Posts post = findById(postId);

        if(!Objects.equals(post.getUsers().getId(), currentUser.getId())){
            throw new CustomException(ErrorCode.UNAUTHORIZED.getCode(), "다른 유저의 게시글에 업로드 할 수 없습니다.");
        }

        // 기존 이미지
        Set<PostImage> oldPostImages = post.getImages();

        // 기존 이미지 중 삭제 할 ID 목록 필터링
        Set<PostImage> toRemove = oldPostImages.stream()
                .filter(img -> deleteImageIds.contains(img.getId()))
                .collect(Collectors.toSet());

        // 제거 처리
        String postPath = UploadPaths.POST.getPath();
        for (PostImage postImage : toRemove) {
            post.removeImage(postImage); // orphanRemoval로 인해 DB에서도 자동 삭제
            imageService.deleteOldImage(postPath + postImage.getImageName()); // 실제 파일 삭제
        }

        return postsMapper.toDto(post).getImages();
    }


    public List<PostsDto> searchPosts(String title, String fishType) {
        return postsRepository.searchPosts(title, fishType).stream()
                .map(postsMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 수정
     * @param postId 게시글 아이디
     * @param postsDto 게시글 수정 요청 객체
     * @param currentUser 현재 접속한 유저 정보 객체
     * @return PostsDTO 객체
     */
    @Transactional
    public PostsDto updatePost(Long postId, PostsUpdateRequestDto postsDto, Users currentUser) {

        Posts post = findById(postId);

        if (!post.getUsers().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("작성자만 수정 가능합니다.");
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

        return postsMapper.toDtoWithoutImage(updatedPost);
    }

    /**
     * 게시글 삭제
     * @param postId 게시글 아이디
     * @param currentUser 현재 접속한 유저 정보 객체
     */
    @Transactional
    public void deletePost(Long postId, Users currentUser) {

        Posts post = findById(postId);

        if (!post.getUsers().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("작성자만 수정 가능합니다.");
        }

        postsLikeRepository.deleteAllByPostId(postId);
        postsRepository.delete(post);
    }

    public Posts findById(long postId) {
        return postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    }

    /**
     * 게시글 상세보기
     * @param postId 게시글 아이디
     * @param redisUserKey 현재 접속한 유저 아이디 혹은 아이피
     * @return PostDTO 객체
     */
    public PostsDto getPostDetail(Long postId, String redisUserKey) {
        Posts post = findById(postId);

        // Redis 조회 키 생성
        String redisKey = "postView:" + postId + ":" + redisUserKey;

        // 3. Redis에 해당 키가 존재하는 지 확인
        if (!redisService.exists(redisKey)) {
            // 키가 없으면 -> 조회수 증가 로직
            post.increaseViewCount();
            postsRepository.save(post);

            // Redis에 저장 + 만료 시간(4시간) 설정
            redisService.setWithExpire(redisKey, true, 4, TimeUnit.HOURS);
        }

        // 게시물을 DTO로 변환하여 반환
        return postsMapper.toDto(post);
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
