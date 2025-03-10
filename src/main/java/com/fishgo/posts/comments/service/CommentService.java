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
import org.springframework.security.core.context.SecurityContextHolder;
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

    // 최적화된 댓글 조회
    public List<CommentResponseDto> getCommentsByPostId(Long postId) throws Exception{

        // 1) 댓글 목록 불러오기
        List<Comment> comments = commentRepository.findByPostIdWithReplies(postId);
        // 2) 매퍼로 DTO 변환
        List<CommentResponseDto> responses = commentMapper.toResponseList(comments);

        // 3) 현재 로그인 사용자
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 4) 댓글 좋아요 여부 재귀함수
        fillLikeStatusRecursively(responses, currentUser.getId());

        return responses;

    }

    // 댓글 작성
    public CommentResponseDto saveComment(CommentCreateRequestDto dto) {

        Comment comment = commentMapper.toCreateEntity(dto);
        // 현재 로그인 사용자 정보 가져오기
        Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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

    public CommentResponseDto updateComment(CommentUpdateRequestDto dto) {
        // 현재 로그인 사용자 정보 가져오기
        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 1) DB에서 댓글 엔티티 조회
        Comment comment = commentRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 2) 작성자(userId)와 현재 사용자(userId)가 일치하는지 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.setContents(dto.getContents());
        // 4) JpaRepository는 @Transactional 환경에서 엔티티 필드가 변경되면 자동으로 UPDATE 처리됨
        // 필요하다면 comment = commentRepository.save(comment); 코드로 명시적 업데이트도 가능합니다.

        return commentMapper.toResponse(comment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId) {
        Users user = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재 하지 않습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }
        // 연결된 좋아요(CommentLike) 정보 먼저 삭제
        commentLikeRepository.deleteAllByCommentId(commentId);

        commentRepository.deleteById(commentId);
    }

    // 댓글 좋아요 여부 판별
    private void fillLikeStatusRecursively(List<CommentResponseDto> commentResponsDtos, Long userId) {
        for (CommentResponseDto resp : commentResponsDtos) {
            boolean liked = commentLikeRepository.existsByCommentIdAndUserId(resp.getId(), userId);
            resp.setLiked(liked);

            // 자식 댓글이 있다면 재귀적으로 처리
            if (resp.getReplies() != null && !resp.getReplies().isEmpty()) {
                fillLikeStatusRecursively(resp.getReplies(), userId);
            }
        }
    }
}
