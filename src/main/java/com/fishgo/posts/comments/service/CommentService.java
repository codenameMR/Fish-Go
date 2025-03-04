package com.fishgo.posts.comments.service;

import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.dto.CommentDto;
import com.fishgo.posts.comments.repository.CommentRepository;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.mapper.CommentMapper;
import com.fishgo.posts.service.PostsService;
import com.fishgo.users.domain.Users;
import com.fishgo.users.service.UsersService;
import lombok.RequiredArgsConstructor;
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

    // 최적화된 댓글 조회
    public List<Comment> getCommentsByPostId(Long postId) throws Exception{
        return commentRepository.findByPostIdWithReplies(postId);
    }

    // 댓글 작성
    public CommentDto saveComment(CommentDto dto) {

        Comment comment = commentMapper.toEntity(dto);

        Users user = usersService.findByUserId(dto.getUserId());
        comment.setUser(user);

        Posts post = postsService.findById(dto.getPostsId());
        comment.setPost(post);

        if (dto.getParentId() != null) {
            Comment parentComment = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
            comment.setParent(parentComment);
        }

        Comment saved = commentRepository.save(comment);

        return commentMapper.toDto(commentRepository.save(saved));
    }

    public CommentDto updateComment(CommentDto dto) {
        // 1) DB에서 댓글 엔티티 조회
        Comment comment = commentRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 2) 작성자(userId)와 현재 사용자(userId)가 일치하는지 확인 → 선택 사항
        if (!comment.getUser().getId().equals(dto.getUserId())) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.setContents(dto.getContents());

        // 4) JpaRepository는 @Transactional 환경에서 엔티티 필드가 변경되면 자동으로 UPDATE 처리됨
        // 필요하다면 comment = commentRepository.save(comment); 코드로 명시적 업데이트도 가능합니다.

        return commentMapper.toDto(comment);
    }


    // 댓글 삭제
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
