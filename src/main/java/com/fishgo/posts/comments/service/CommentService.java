package com.fishgo.posts.comments.service;

import com.fishgo.badge.event.CommentCreatedEvent;
import com.fishgo.common.service.PageService;
import com.fishgo.posts.comments.domain.Comment;
import com.fishgo.posts.comments.domain.CommentMention;
import com.fishgo.posts.comments.dto.*;
import com.fishgo.posts.comments.dto.mapper.CommentMapper;
import com.fishgo.posts.comments.dto.projection.ParentCommentProjection;
import com.fishgo.posts.comments.repository.CommentLikeRepository;
import com.fishgo.posts.comments.repository.CommentRepository;
import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.service.PostsService;
import com.fishgo.users.domain.Users;
import com.fishgo.users.repository.UsersRepository;
import com.fishgo.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UsersRepository usersRepository;
    private final UsersService usersService;
    private final PageService pageService;
    private final PostsService postsService;
    private final CommentMapper commentMapper;
    private final CommentLikeRepository commentLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 댓글 및 해당 댓글의 첫번째 댓글, 그리고 남은 대댓글 개수를 반환한다.
     * @param postId 게시글 아이디
     * @param pageable 페이지에이블 객체
     * @param currentUser 현재 접속 중인 유저 객체
     * @return 페이지에이블 객체
     */
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getParentCommentsWithFirstReply(Long postId, Pageable pageable, Users currentUser) {

        // 1) Projection으로 조회
        Page<ParentCommentProjection> projectionPage =
                commentRepository.findParentCommentsWithFirstReply(postId, pageable);

        // 2) Projection -> DTO 변환
        Page<CommentResponseDto> dtoPage = projectionPage.map(projection -> {

            // 2-1) 부모 댓글 기본 정보 셋팅
            CommentResponseDto parentDto = commentMapper.projectionToResponse(projection);

            // 남은 대댓글 수
            parentDto.setRemainingReplyCount(
                    projection.getRemainingReplyCount() == null ? 0 : projection.getRemainingReplyCount()
            );

            // 2-2) 첫 번째 대댓글이 있는 경우, 실제 댓글 엔티티를 다시 조회해 Mapper로 변환
            if (projection.getFirstReplyId() != null) {
                Comment firstReply = commentRepository.findById(projection.getFirstReplyId())
                        .orElse(null);
                if (firstReply != null) {
                    ReplyResponseDto replyDto = commentMapper.toReplyResponse(firstReply);
                    parentDto.setFirstReply(replyDto);
                }
            }

            return parentDto;
        });

        // 3) 좋아요 여부
        if(currentUser != null){
            fillLikeStatusRecursively(dtoPage, currentUser.getId());
        }

        return dtoPage;
    }

    /**
     * 특정 댓글(parentId)의 대댓글"만 별도로 페이지네이션하는 메서드.
     * @param parentId 부모 댓글 아이디
     * @param pageable 페이지에이블 객체
     * @return 페이지에이블 객체
     */
    @Transactional(readOnly = true)
    public RepliesResponseDto getReplies(Long parentId, Pageable pageable, Users currentUser) {
        Page<Comment> replies = commentRepository.findAllByParentId(parentId, pageable);

        long remainingRepliesCount = pageService.getRemainingCount(replies);

        Page<ReplyResponseDto> replyDtoPage = replies.map(commentMapper::toReplyResponse);

        // 댓글 가져올 때 첫번째 대댓글을 같이 가져오므로
        // 첫 페이지(0번)일 경우 첫 번째 대댓글 제거
        if(pageable.getPageNumber() == 0 && !replyDtoPage.isEmpty()) {
            // 기존 Page의 내용을 List로 복사
            List<ReplyResponseDto> contentList = new ArrayList<>(replyDtoPage.getContent());
            contentList.removeFirst(); // 첫 번째 댓글 제거

            // 수정된 List를 다시 Page로 감싼다. totalElements는 기존과 동일하게 사용 가능
            replyDtoPage = new PageImpl<>(contentList, pageable, replies.getTotalElements());
        }


        if(currentUser != null) {
            fillLikeStatusForReplies(replyDtoPage, currentUser.getId());
        }


        return RepliesResponseDto.builder()
                .replies(replyDtoPage)
                .remainingRepliesCount(remainingRepliesCount)
                .build();
    }


    /**
     * 댓글 작성
     * @param dto 댓글 작성 요청 객체
     * @param currentUser 현재 접속 중인 유저 객체
     * @return 댓글 응답 객체
     */
    @Transactional
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

        // 멘션된 유저 등록
        Users mentionedUser = (dto.getMentionedUserId() != null)
                ? usersRepository.findById(dto.getMentionedUserId())
                .orElseThrow(() -> new IllegalArgumentException("멘션 된 사용자의 정보를 찾을 수 없습니다."))
                : null;
        comment.setMention(new CommentMention(comment, mentionedUser));

        Comment savedComment = commentRepository.save(comment);

        // 뱃지 이벤트
        eventPublisher.publishEvent(new CommentCreatedEvent(savedComment));

        return commentMapper.toResponse(comment);
    }

    /**
     * 댓글 수정
     * @param dto 댓글 수정 요청 객체
     * @param currentUser 현재 접속 중인 유저 객체
     * @return 댓글 응답 객체
     */
    @Transactional
    public CommentResponseDto updateComment(CommentUpdateRequestDto dto, Users currentUser) {
        // 1) DB에서 댓글 엔티티 조회
        Comment comment = commentRepository.findById(dto.getCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 2) 작성자(userId)와 현재 사용자(userId)가 일치하는지 확인
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 3) JpaRepository는 @Transactional 환경에서 엔티티 필드가 변경되면 자동으로 UPDATE 처리됨
        // 필요하다면 comment = commentRepository.save(comment); 코드로 명시적 업데이트도 가능합니다.
        comment.setContents(dto.getContents());

        Users mentionedUser = (dto.getMentionedUserId() != null) ?
                usersRepository.findById(dto.getMentionedUserId())
                        .orElseThrow(() -> new IllegalArgumentException("멘션 된 사용자의 정보를 찾을 수 없습니다."))
                : null;
        comment.updateMention(mentionedUser);

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
            if (resp.getFirstReply() != null) {
                fillLikeStatusRecursively(resp.getFirstReply(), userId);
            }
        }
    }

    private void fillLikeStatusForReplies(Page<ReplyResponseDto> list, Long userId) {
        for (ReplyResponseDto resp : list) {
            boolean liked = commentLikeRepository.existsByCommentIdAndUserId(resp.getId(), userId);
            resp.setLiked(liked);

        }
    }

    private void fillLikeStatusRecursively(ReplyResponseDto dto, Long userId) {
            boolean liked = commentLikeRepository.existsByCommentIdAndUserId(dto.getId(), userId);
            dto.setLiked(liked);

    }

}
