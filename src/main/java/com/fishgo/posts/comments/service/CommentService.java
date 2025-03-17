package com.fishgo.posts.comments.service;

import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentCreateRequestDto;
import com.fishgo.posts.comments.dto.CommentResponseDto;
import com.fishgo.posts.comments.dto.CommentUpdateRequestDto;
import com.fishgo.posts.comments.dto.mapper.CommentMapper;
import com.fishgo.posts.comments.repository.CommentLikeRepository;
import com.fishgo.posts.comments.repository.CommentRepository;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.service.PostsService;
import com.fishgo.users.domain.Users;
import com.fishgo.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UsersService usersService;
    private final PostsService postsService;
    private final CommentMapper commentMapper;
    private final CommentLikeRepository commentLikeRepository;

    /**
     *  댓글 조회
     * @param postId 게시글 아이디
     * @param pageable 페이지네이션 객체
     * @param currentUser 현재 접속 중인 유저
     * @return 페이지네이션 된 댓글 응답 객체
     */
    @Transactional
    public Page<CommentResponseDto> getCommentsByPostId(Long postId, Pageable pageable, Users currentUser) {

        // 1) 부모만 우선 페이징 조회
        Page<Comment> commentPage = commentRepository.findByPostIdWithoutReplies(postId, pageable);
        // 2) 페이징된 부모 목록 추출
        List<Comment> parentComments = commentPage.getContent();
        if (parentComments.isEmpty()) {
            // 부모가 없으면 굳이 자식 로딩 쿼리 불필요
            return commentPage.map(commentMapper::toResponse);
        }

        // 3) 부모 IDs 추출
        List<Long> commentIds = parentComments
                .stream()
                .map(Comment::getId)
                .toList();

        // 4) 자식까지 fetch join
        // DB에서 조회된 엔티티의 식별자(Primary Key)가 같으면, 같은 레코드를 가리키는 엔티티이므로
        // JPA 영속성 컨텍스트 입장에서는 동일한 엔티티 객체로 인식되기 때문에
        // 이 때 commentPage 객체가 자식을 가진 상태로 업데이트 됨.
        commentRepository.findCommentsWithReplies(commentIds);

        // 5) 매퍼로 DTO 변환
        Page<CommentResponseDto> responses = commentPage.map(commentMapper::toResponse);

        // 6) 댓글 좋아요 여부 재귀함수
        if(currentUser != null) {
            fillLikeStatusRecursively(responses, currentUser.getId());
        }

        return responses;

    }

    /**
     * 댓글 작성
     * @param dto 댓글 작성 요청 객체
     * @param currentUser 현재 접속 중인 유저 객체
     * @return 댓글 응답 객체
     */
    public CommentResponseDto saveComment(CommentCreateRequestDto dto, Users currentUser) {

        Comment comment = commentMapper.toCreateEntity(dto);

        Users user = usersService.findByUserId(currentUser.getId());
        comment.setUser(user);

        Posts post = postsService.findById(dto.getPostId());
        comment.setPost(post);

        if (dto.getParentId() != null) {
            Comment parentComment = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
            comment.setParent(parentComment);
        }

        Comment saved = commentRepository.save(comment);

        return commentMapper.toResponse(saved);
    }

    /**
     * 댓글 수정
     * @param dto 댓글 수정 요청 객체
     * @param currentUser 현재 접속 중인 유저 객체
     * @return 댓글 응답 객체
     */
    public CommentResponseDto updateComment(CommentUpdateRequestDto dto, Users currentUser) {
        // 1) DB에서 댓글 엔티티 조회
        Comment comment = commentRepository.findById(dto.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 2) 작성자(userId)와 현재 사용자(userId)가 일치하는지 확인
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.setContents(dto.getContents());
        // 4) JpaRepository는 @Transactional 환경에서 엔티티 필드가 변경되면 자동으로 UPDATE 처리됨
        // 필요하다면 comment = commentRepository.save(comment); 코드로 명시적 업데이트도 가능합니다.

        return commentMapper.toResponse(comment);
    }

    /**
     * 댓글 삭제
     * @param commentId 댓글 아이디
     * @param currentUser 현재 접속중인 유저 객체
     */
    public void deleteComment(Long commentId, Users currentUser) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재 하지 않습니다."));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }
        // 연결된 좋아요(CommentLike) 정보 먼저 삭제
        commentLikeRepository.deleteAllByCommentId(commentId);

        commentRepository.deleteById(commentId);
    }

    /**
     * 댓글 좋아요 여부 판별
     * @param list 조회가 완료된 페이지네이션 댓글 응답 객체
     * @param userId 유저 아이디
     */
    private void fillLikeStatusRecursively(Page<CommentResponseDto> list, Long userId) {
        for (CommentResponseDto resp : list) {
            boolean liked = commentLikeRepository.existsByCommentIdAndUserId(resp.getId(), userId);
            resp.setLiked(liked);

            // 자식 댓글이 있다면 재귀적으로 처리
            if (resp.getReplies() != null && !resp.getReplies().isEmpty()) {
                fillLikeStatusRecursively(resp.getReplies(), userId);
            }
        }
    }

    // 댓글 좋아요 여부 판별 오버로딩
    private void fillLikeStatusRecursively(List<CommentResponseDto> list, Long userId) {
        for (CommentResponseDto resp : list) {
            boolean liked = commentLikeRepository.existsByCommentIdAndUserId(resp.getId(), userId);
            resp.setLiked(liked);

            // 자식의 자식 댓글이 또 List이면 동일하게 재귀
            if (resp.getReplies() != null && !resp.getReplies().isEmpty()) {
                fillLikeStatusRecursively(resp.getReplies(), userId);
            }
        }
    }

}
