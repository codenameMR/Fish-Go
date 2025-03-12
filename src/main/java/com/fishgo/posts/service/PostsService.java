package com.fishgo.posts.service;

import com.fishgo.common.constants.UploadPaths;
import com.fishgo.common.service.ImageService;
import com.fishgo.posts.domain.Hashtag;
import com.fishgo.posts.domain.PostImage;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileSystemException;
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
    private final ImageService imageService;

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
     * @param files 게시글 이미지 리스트
     * @param user 현재 로그인한 유저 정보 객체
     * @return PostsDTO 객체
     */
    @Transactional
    public PostsDto createPost(PostsCreateRequestDto postsDto, List<MultipartFile> files, Users user) throws FileSystemException {

        // Hashtag 처리
        Set<Hashtag> hashtags = processHashtags(postsDto.getHashTag());

        // 3) Posts 엔티티 생성
        Posts newPost = postsMapper.toEntity(postsDto);
        newPost.setUsers(user);
        newPost.setHashtag(hashtags);

        // 4) DB 저장
        postsRepository.save(newPost);

        // 5) 이미지 경로에 postId를 넣기 위해 이미지를 후처리함.
        imageService.handleCreatePostImageUpload(newPost, files);
        // 5-1) 이미지 아이디를 가지고 오기 위해 flush처리
        postsRepository.flush();

        // 6) 결과 DTO 반환
        return postsMapper.toDto(newPost);

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
     * @param newImages 변경된 새 이미지 리스트
     * @param currentUser 현재 접속한 유저 정보 객체
     * @return PostsDTO 객체
     */
    @Transactional
    public PostsDto updatePost(Long postId, PostsUpdateRequestDto postsDto, List<MultipartFile> newImages, Users currentUser) throws FileSystemException {

        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!post.getUsers().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("작성자만 수정 가능합니다.");
        }

        // 기존 이미지
        Set<PostImage> oldPostImages = post.getImages();

        // 기존 이미지 중 “유지할 ID 목록”을 제외하고 나머지는 제거
        if (postsDto.getExistingImageIds() != null) {
            Set<Long> keepIds = new HashSet<>(postsDto.getExistingImageIds());
            // 제거 대상 식별
            Set<PostImage> toRemove = oldPostImages.stream()
                    .filter(img -> !keepIds.contains(img.getId()))
                    .collect(Collectors.toSet());
            // 제거 처리
            String postPath = UploadPaths.POST.getPath();
            for (PostImage postImage : toRemove) {
                post.removeImage(postImage); // orphanRemoval로 인해 DB에서도 자동 삭제
                imageService.deleteOldImage(postPath + postImage.getImageName()); // 실제 파일 삭제
            }
        }

        // 새로 업로드할 이미지 처리
        if (newImages != null && !newImages.isEmpty()) {
            if (imageService.isImageFile(newImages)){

                for (MultipartFile file : newImages) {
                    // 파일 저장 후 Image 생성
                    String savedPath = imageService.uploadPostImage(file, post.getId());

                    // 새 Image 엔티티 생성 후 Post에 추가
                    PostImage newPostImage = new PostImage();
                    newPostImage.setImageName(savedPath);
                    post.addImage(newPostImage);  // Post.addImage() 내부에서 newImage.setPost(this)도 호출

                }
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
     * @return PostDTO 객체
     */
    public PostsDto getPostDetail(Long postId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));
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
