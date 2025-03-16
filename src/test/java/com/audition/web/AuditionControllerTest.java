package com.audition.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuditionControllerTest {

    @Mock
    AuditionService auditionService;

    @InjectMocks
    AuditionController auditionController;

    @Test
    void testGetPosts() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title", "Post Body", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(1, null, null, null);
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByUserId() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title", "Post Body", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title", "Post Body", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(1, null, null, null);
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByPostId() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title", "Post Body", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title", "Post Body", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(null, 1, null, null);
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByPostTitle() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title AAA", "Post Body", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title BBB", "Post Body", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(null, null, "AAA", null);
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByPostBody() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title AAA", "Post Body AAA", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title BBB", "Post Body BBB", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(null, null, null, "BBB");
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByPostCombined() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title AAA", "Post Body AAA", new ArrayList<>()),
            new AuditionPost(11, 1, "Post Title", "Post Body AAA", new ArrayList<>()),
            new AuditionPost(1, 12, "Post Title", "Post Body AAA", new ArrayList<>()),
            new AuditionPost(1, 12, "Post Title", "Post Body", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title BBB", "Post Body BBB", new ArrayList<>()));

        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(1, 1, "AAA", "AAA");
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPost() {
        AuditionPost auditionPost = new AuditionPost(1, 1, "Post Title", "Post Body", new ArrayList<>());
        when(auditionService.getPostById("1")).thenReturn(auditionPost);
        AuditionPost resultAuditionPost = auditionController.getPost("1");
        Assertions.assertEquals(1, resultAuditionPost.getId());
        Assertions.assertEquals(1, resultAuditionPost.getUserId());
        Assertions.assertEquals("Post Title", resultAuditionPost.getTitle());
        Assertions.assertEquals("Post Body", resultAuditionPost.getBody());
    }

    @Test
    void testGetPostExceptionScenario() {
        AuditionPost auditionPost = new AuditionPost(1, 1, "Post Title", "Post Body", new ArrayList<>());
        when(auditionService.getPostById(anyString())).thenThrow(
            new SystemException(String.format("Invalid postId: %s", auditionPost.getId())));
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionController.getPost("xxxxx"));
        Assertions.assertEquals("Invalid postId: xxxxx, Invalid number For input string: \"xxxxx\"",
            systemException.getMessage());
    }

    @Test
    void getPostWithComments() {
        AuditionPost auditionPost = new AuditionPost(1, 1, "Post Title", "Post Body",
            List.of(new Comment(1, 1, "name", "email.@email.com", "body")));
        when(auditionService.getPostWithComments("1")).thenReturn(auditionPost);
        AuditionPost resultAuditionPost = auditionController.getPostWithComments("1");
        Assertions.assertEquals(1, resultAuditionPost.getId());
        Assertions.assertEquals(1, resultAuditionPost.getUserId());
        Assertions.assertEquals("Post Title", resultAuditionPost.getTitle());
        Assertions.assertEquals("Post Body", resultAuditionPost.getBody());
        //Comment Veririfcations
        Assertions.assertEquals(1, resultAuditionPost.getComments().size());
        Assertions.assertEquals("name", resultAuditionPost.getComments().get(0).getName());
        Assertions.assertEquals("email.@email.com", resultAuditionPost.getComments().get(0).getEmail());
        Assertions.assertEquals("body", resultAuditionPost.getComments().get(0).getBody());
    }

    @Test
    void testGetPostWithCommentsExceptionScenario() {
        when(auditionService.getPostWithComments(anyString())).thenThrow(
            new SystemException(String.format("Invalid postId: %s", "xxxxx")));
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionController.getPostWithComments("xxxxx"));
        Assertions.assertEquals("Invalid postId: xxxxx, Invalid number For input string: \"xxxxx\"",
            systemException.getMessage());
    }

    @Test
    void testGetPostComments() {
        List<Comment> comments = List.of(new Comment(1, 1, "name", "email.@email.com", "body"),
            new Comment(1, 2, "name2", "email2.@email.com", "body2"));
        when(auditionService.getPostComments("1")).thenReturn(comments);

        List<Comment> resultComments = auditionController.getPostComments("1");
        Assertions.assertEquals(2, resultComments.size());
        Assertions.assertEquals(1, resultComments.get(0).getId());
        Assertions.assertEquals(2, resultComments.get(1).getId());
    }

    @Test
    void testGetPostCommentsExceptionScenario() {
        when(auditionService.getPostComments(anyString())).thenThrow(
            new SystemException(String.format("Invalid postId: %s", "xxxxx")));

        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionController.getPostComments("xxxxx"));
        Assertions.assertEquals("Invalid postId: xxxxx, Invalid number For input string: \"xxxxx\"",
            systemException.getMessage());
    }
}