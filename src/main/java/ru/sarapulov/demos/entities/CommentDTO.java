package ru.sarapulov.demos.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sarapulov.demos.models.Comment;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CommentDTO {

    private String login;

    private Instant time;

    private String commentText;

    public static CommentDTO getDTOFrom(Comment comment) {
        return CommentDTO.builder()
                         .login(comment.getSender()
                                       .getUser()
                                       .getLogin())
                         .commentText(comment.getCommentText())
                         .time(comment.getTime())
                         .build();
    }

}
