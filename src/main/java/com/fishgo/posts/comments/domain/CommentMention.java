package com.fishgo.posts.comments.domain;

import com.fishgo.users.domain.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id")
    private Users mentionedUser;

    public CommentMention(Comment comment, Users user) {
        this.comment = comment;
        this.mentionedUser = user;
    }

    protected CommentMention() {}
}
