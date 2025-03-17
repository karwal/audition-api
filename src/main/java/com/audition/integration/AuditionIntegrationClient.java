package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.tracing.annotation.NewSpan;
import java.util.List;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@EnableConfigurationProperties
public class AuditionIntegrationClient {

    private RestTemplate restTemplate;
    private final static String RESOURCE_POST = "posts";

    @SuppressFBWarnings
    public AuditionIntegrationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${spring.application.externalApiUrl}")
    @Setter(onMethod_ = @SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"}))
    private String externalApiUrl;

    @NewSpan("Invoke external api endpoint /posts")
    public List<AuditionPost> getPosts() {
        // TODO-Resolved make RestTemplate call to get Posts from https://jsonplaceholder.typicode.com/posts
        try {

            ResponseEntity<List<AuditionPost>> auditionResponse =
                restTemplate.exchange(String.format("%s/%s", externalApiUrl, RESOURCE_POST),
                    HttpMethod.GET, getEntityWithHeaders(), new ParameterizedTypeReference<List<AuditionPost>>() {
                    });
            List<AuditionPost> auditionList = auditionResponse.getBody();
            return auditionList;
        } catch (final HttpClientErrorException e) {
            throw new SystemException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
        }
    }

    @NewSpan("Invoke external api endpoint /posts/{id}")
    public AuditionPost getPostById(final String id) {
        // TODO-Resolved get post by post ID call from https://jsonplaceholder.typicode.com/posts/
        try {
            ResponseEntity<AuditionPost> auditionPostEntity =
                restTemplate.getForEntity(String.format("%s/%s/%s", externalApiUrl, RESOURCE_POST, id),
                    AuditionPost.class);
            return auditionPostEntity.getBody();
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + id, "Resource Not Found", 404);
            } else {
                // TODO-Resolved Find a better way to handle the exception so that the original error message is not lost. Feel free to change this function.
                throw new SystemException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
            }
        }
    }

    // TODO-Resolved Write a method GET comments for a post from https://jsonplaceholder.typicode.com/posts/{postId}/comments - the comments must be returned as part of the post.
    @NewSpan("Invoke external api endpoint /posts/{postId}/comments")
    public AuditionPost getPostWithComments(final String postId) {
        // TODO-Resolved get post by post ID call from https://jsonplaceholder.typicode.com/posts/
        try {
            ResponseEntity<List<Comment>> postCommentsResponse =
                restTemplate.exchange(String.format("%s/%s/%s/%s", externalApiUrl, RESOURCE_POST, postId, "comments"),
                    HttpMethod.GET, getEntityWithHeaders(), new ParameterizedTypeReference<>() {
                    });
            AuditionPost auditionPost = getPostById(postId);
            auditionPost.setComments(postCommentsResponse.getBody());
            return auditionPost;
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find comments for a Post with id " + postId, "Resource Not Found",
                    404);
            } else {
                // TODO-Resolved Find a better way to handle the exception so that the original error message is not lost. Feel free to change this function.
                throw new SystemException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
            }
        }
    }

    // TODO-Resolved write a method. GET comments for a particular Post from https://jsonplaceholder.typicode.com/comments?postId={postId}.
    // The comments are a separate list that needs to be returned to the API consumers. Hint: this is not part of the AuditionPost pojo.
    @NewSpan("Invoke external api endpoint /posts/{postId}/comments")
    public List<Comment> getPostComments(final String postId) {
        // TODO-Resolved get post by post ID call from https://jsonplaceholder.typicode.com/posts/
        try {
            ResponseEntity<List<Comment>> postCommentsResponse =
                restTemplate.exchange(String.format("%s/%s/%s/%s", externalApiUrl, RESOURCE_POST, postId, "comments"),
                    HttpMethod.GET, getEntityWithHeaders(), new ParameterizedTypeReference<List<Comment>>() {
                    });
            return postCommentsResponse.getBody();
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find comments for a Post with id " + postId, "Resource Not Found",
                    404);
            } else {
                // TODO-Resolved Find a better way to handle the exception so that the original error message is not lost. Feel free to change this function.
                throw new SystemException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
            }
        }
    }

    private HttpEntity<String> getEntityWithHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Authorization");
        headers.set("Other Header", "Some other header");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return entity;
    }
}
