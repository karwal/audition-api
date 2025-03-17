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

    private final static String INVALID_NUMBER = "xxxx";

    @Test
    void testGetPosts() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title 1", "Post Body 1", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(1, null, null, null);
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByUserId() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title 2", "Post Body 2", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title 3", "Post Body 3", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(1, null, null, null);
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByPostId() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title 4", "Post Body 4", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title 5", "Post Body 5", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(null, 1, null, null);
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByPostTitle() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title AAA", "Post Body A", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title BBB", "Post Body B", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(null, null, "AAA", null);
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByPostBody() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title A", "Post Body AAA", new ArrayList<>()),
            new AuditionPost(2, 2, "Post Title B", "Post Body BBB", new ArrayList<>()));
        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(null, null, null, "BBB");
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPostsFilteringByPostCombined() {
        List<AuditionPost> auditionPosts = List.of(
            new AuditionPost(1, 1, "Post Title AAAA", "Post Body BBBB", new ArrayList<>()),
            new AuditionPost(11, 1, "Post Title 11", "Post Body 11", new ArrayList<>()),
            new AuditionPost(12, 12, "Post Title 12", "Post Body 12", new ArrayList<>()),
            new AuditionPost(13, 13, "Post Title 13", "Post Body 13", new ArrayList<>()),
            new AuditionPost(14, 14, "Post Title 14", "Post Body 14", new ArrayList<>()));

        when(auditionService.getPosts()).thenReturn(auditionPosts);
        List<AuditionPost> filteredAuditionPosts = auditionController.getPosts(1, 1, "AAAA", "BBBB");
        Assertions.assertEquals(1, filteredAuditionPosts.size());
    }

    @Test
    void testGetPost() {
        AuditionPost auditionPost = new AuditionPost(1, 1, "Post Title 15", "Post Body 15", new ArrayList<>());
        when(auditionService.getPostById("1")).thenReturn(auditionPost);
        AuditionPost resultAuditionPost = auditionController.getPost("1");
        Assertions.assertEquals(1, resultAuditionPost.getId());
        Assertions.assertEquals(1, resultAuditionPost.getUserId());
        Assertions.assertEquals("Post Title 15", resultAuditionPost.getTitle());
        Assertions.assertEquals("Post Body 15", resultAuditionPost.getBody());
    }

    @Test
    void testGetPostExceptionScenario() {
        when(auditionService.getPostById(anyString())).thenThrow(
            new SystemException(String.format("Invalid postId: %s", INVALID_NUMBER)));
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionController.getPost(INVALID_NUMBER));
        Assertions.assertEquals("Invalid postId: xxxx, Invalid number For input string: \"xxxx\"",
            systemException.getMessage());
    }

    @Test
    void getPostWithComments() {
        AuditionPost auditionPost = new AuditionPost(1, 1, "Post Title 16", "Post Body 16",
            List.of(new Comment(1, 1, "name", "email.@email.com", "body")));
        when(auditionService.getPostWithComments("1")).thenReturn(auditionPost);
        AuditionPost resultAuditionPost = auditionController.getPostWithComments("1");
        Assertions.assertEquals(1, resultAuditionPost.getId());
        Assertions.assertEquals(1, resultAuditionPost.getUserId());
        Assertions.assertEquals("Post Title 16", resultAuditionPost.getTitle());
        Assertions.assertEquals("Post Body 16", resultAuditionPost.getBody());
        //Comment Veririfcations
        Assertions.assertEquals(1, resultAuditionPost.getComments().size());
        Assertions.assertEquals("name", resultAuditionPost.getComments().get(0).getName());
        Assertions.assertEquals("email.@email.com", resultAuditionPost.getComments().get(0).getEmail());
        Assertions.assertEquals("body", resultAuditionPost.getComments().get(0).getBody());
    }

    @Test
    void testGetPostWithCommentsExceptionScenario() {
        when(auditionService.getPostWithComments(anyString())).thenThrow(
            new SystemException(String.format("Invalid postId: %s", INVALID_NUMBER)));
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionController.getPostWithComments(INVALID_NUMBER));
        Assertions.assertEquals("Invalid postId: xxxx, Invalid number For input string: \"xxxx\"",
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
            new SystemException(String.format("Invalid postId: %s", INVALID_NUMBER)));

        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionController.getPostComments(INVALID_NUMBER));
        Assertions.assertEquals("Invalid postId: xxxx, Invalid number For input string: \"xxxx\"",
            systemException.getMessage());
    }
}