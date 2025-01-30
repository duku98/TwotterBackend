package com.akaci.twotterbackend.application.controller;

import com.akaci.twotterbackend.application.dto.response.comment.CommentsOfTwootResponse;
import com.akaci.twotterbackend.application.dto.response.like.LikeResponse;
import com.akaci.twotterbackend.application.service.LikeService;
import com.akaci.twotterbackend.application.service.crud.CommentCrudService;
import jakarta.websocket.server.PathParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api")
public class CommentController {

    private static final Logger logger = LogManager.getLogger(CommentController.class);

    private final LikeService commentLikeService;
    private final CommentCrudService commentCrudService;

    public CommentController(@Qualifier("comment-like-service") LikeService likeService, CommentCrudService commentCrudService) {
        this.commentLikeService = likeService;
        this.commentCrudService = commentCrudService;
    }

    @GetMapping("comment")
    public ResponseEntity<CommentsOfTwootResponse> getCommentsOfTwoot(@RequestParam("twoot_id") UUID twootId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CommentsOfTwootResponse response = commentCrudService.getCommentsOfTwoot(twootId, auth.getName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping("public/comment")
    public ResponseEntity<CommentsOfTwootResponse> getCommentsOfTwootAuth(@RequestParam("twoot_id") UUID twootId) {
        CommentsOfTwootResponse response = commentCrudService.getCommentsOfTwoot(twootId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("comment/{id}/like")
    public ResponseEntity<LikeResponse> likeComment(@PathVariable("id") UUID commentId) {
        String username = getAccountUsername();
        LikeResponse response = commentLikeService.like(username, commentId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    private String getAccountUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("could not get authentication");
        }
        return auth.getName();
    }

}
