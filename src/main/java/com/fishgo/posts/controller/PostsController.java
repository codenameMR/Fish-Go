package com.fishgo.posts.controller;

import com.fishgo.posts.domain.Posts;
import com.fishgo.posts.dto.PostsDto;
import com.fishgo.posts.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostsController {

    private static final Logger logger = LoggerFactory.getLogger(PostsController.class);
    private final PostsService postsService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody PostsDto postsDto) {

        try {
            Posts savedPost = postsService.createPost(postsDto);
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("저장실패");
        }

    }

    @GetMapping("/search")
    public List<PostsDto> search (
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String hashTag,
            @RequestParam(required = false) String fishType
    ) {
        return postsService.searchPosts(title, hashTag, fishType);
    }


    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody PostsDto postsDto) {
        try {
            PostsDto updatedPost = postsService.updatePost(postId, postsDto);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("수정실패");
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            postsService.deletePost(postId);
            return ResponseEntity.ok("삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("삭제실패");
        }
    }

}
