package com.audition.web;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditionController {

    @Autowired
    AuditionService auditionService;

    // TODO-Resolved Add a query param that allows data filtering. The intent of the filter is at developers discretion.
    @RequestMapping(value = "/posts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionPost> getPosts(@Param("userId") Integer userId, @Param("id") Integer id,
        @Param("titleLike") String titleLike, @Param("bodyLike") String bodyLike) {

        // TODO-Resolved Add logic that filters response data based on the query param
        List<AuditionPost> responsePosts = auditionService.getPosts();
        if (Objects.nonNull(userId)) {
            responsePosts = responsePosts.stream().filter(post -> post.getUserId() == userId)
                .collect(Collectors.toList());
        }
        if (Objects.nonNull(id)) {
            responsePosts = responsePosts.stream().filter(post -> post.getId() == id)
                .collect(Collectors.toList());
        }
        if (Objects.nonNull(titleLike)) {
            responsePosts = responsePosts.stream().filter(post -> post.getTitle().contains(titleLike))
                .collect(Collectors.toList());
        }
        if (Objects.nonNull(bodyLike)) {
            responsePosts = responsePosts.stream().filter(post -> post.getBody().contains(bodyLike))
                .collect(Collectors.toList());
        }
        return responsePosts;
    }

    @RequestMapping(value = "/posts/{postId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPost(@NotEmpty @PathVariable("postId") final String postId) {
        // TODO-Resolved Add input validation
        try {
            Integer.valueOf(postId);
            return auditionService.getPostById(postId);
        } catch (NumberFormatException nfe) {
            throw new SystemException(String.format("Invalid postId: %s, Invalid number %s", postId, nfe.getMessage()),
                "Bad Request",
                400);
        }
    }

    // TODO-Resolved Add additional methods to return comments for each post. Hint: Check https://jsonplaceholder.typicode.com/
    @RequestMapping(value = "/posts/{postId}/comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPostWithComments(@NotEmpty @PathVariable("postId") final String postId) {
        try {
            Integer.valueOf(postId);
            return auditionService.getPostWithComments(postId);
        } catch (NumberFormatException nfe) {
            throw new SystemException(String.format("Invalid postId: %s, Invalid number %s", postId, nfe.getMessage()),
                "Bad Request",
                400);
        }
    }

    // TODO-Resolved Add additional methods to return comments for each post. Hint: Check https://jsonplaceholder.typicode.com/
    @RequestMapping(value = "/comments/{postId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Comment> getPostComments(@NotEmpty @PathVariable("postId") final String postId) {
        try {
            Integer.valueOf(postId);
            return auditionService.getPostComments(postId);
        } catch (NumberFormatException nfe) {
            throw new SystemException(String.format("Invalid postId: %s, Invalid number %s", postId, nfe.getMessage()),
                "Bad Request",
                400);
        }
    }

}
