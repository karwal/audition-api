package com.audition.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class AuditionIntegrationClientTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    AuditionIntegrationClient auditionIntegrationClient;

    @Value("${spring.application.externalApiUrl}")
    private String externalApiUrl;

    /*
    Test the external API call for Get Posts
     */
    @Test
    public void testGetPosts() {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(externalApiUrl + "/posts"))
            .andRespond(withSuccess(
                "[{  \"userId\": 1,"
                    + "    \"id\": 1,"
                    + "    \"title\": \"title 1\","
                    + "    \"body\": \"body 1\""
                    + "  },"
                    + "  {"
                    + "    \"userId\": 1,"
                    + "    \"id\": 2,"
                    + "    \"title\": \"title 2\","
                    + "    \"body\": \"body 2\""
                    + "  }]", MediaType.APPLICATION_JSON));
        List<AuditionPost> auditionPostsList = auditionIntegrationClient.getPosts();
        mockServer.verify();
        assertEquals(2, auditionPostsList.size());
        assertEquals("title 1", auditionPostsList.get(0).getTitle());
        assertEquals("body 1", auditionPostsList.get(0).getBody());
        assertEquals(1, auditionPostsList.get(0).getUserId());
        assertEquals(1, auditionPostsList.get(0).getId());
        mockServer.reset();
    }

    /*
    Test the external API call for Get Comments HttpClientErrorException Scenario Other than 404
`   */
    @Test
    public void testGetPostsExceptionScenario() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        String url = String.format("%s/%s", externalApiUrl, "posts");
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        // Verify that the exception is correctly thrown
        AuditionIntegrationClient auditionIntegrationClient = new AuditionIntegrationClient(mockRestTemplate);
        auditionIntegrationClient.setExternalApiUrl(externalApiUrl);
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPosts());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), systemException.getStatusCode());
    }

    /*
    Test the external API call for Get Posts By Id
    */
    @Test
    public void testGetPostById() {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(ExpectedCount.once(), requestTo(externalApiUrl + "/posts/11"))
            .andRespond(withSuccess(
                "{  \"userId\": 1,"
                    + "    \"id\": 11,"
                    + "    \"title\": \"title 1\","
                    + "    \"body\": \"body 1\""
                    + "  }", MediaType.APPLICATION_JSON));

        AuditionPost auditionPost = auditionIntegrationClient.getPostById("11");
        mockServer.verify();
        assertEquals(11, auditionPost.getId());
    }

    /*
    Test HttpClientErrorException Scenario where status code is 404
     */
    @Test
    public void testGetPostByIdExceptionScenario() {
        RestTemplate restTemplateMock = spy(RestTemplate.class);

        when(restTemplateMock.getForEntity(externalApiUrl + "/posts/11", AuditionPost.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        AuditionIntegrationClient auditionIntegrationClient = new AuditionIntegrationClient(restTemplateMock);
        auditionIntegrationClient.setExternalApiUrl(externalApiUrl);
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostById("11"));

        assertEquals(HttpStatus.NOT_FOUND.value(), systemException.getStatusCode());
        assertEquals("Cannot find a Post with id 11", systemException.getDetail());
    }

    /*
    Test Exceptions Scearion for HttpClientErrorException where status code is not 404
     */
    @Test
    public void testGetPostByIdGeneralExceptionScenario() {
        RestTemplate mockRestTemplate = spy(RestTemplate.class);

        when(mockRestTemplate.getForEntity(externalApiUrl + "/posts/11", AuditionPost.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown Error Occurred"));

        AuditionIntegrationClient auditionIntegrationClient = new AuditionIntegrationClient(mockRestTemplate);
        auditionIntegrationClient.setExternalApiUrl(externalApiUrl);
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostById("11"));

        assertEquals("API Error Occurred", systemException.getTitle());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), systemException.getStatusCode());
        assertEquals("500 Unknown Error Occurred", systemException.getDetail());
    }


    /*
    Test the external API call for Get Posts
     */
    @Test
    public void testGetPostWithComments() {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        String postId = "100";
        mockServer.expect(ExpectedCount.once(),
                requestTo(String.format("%s/%s/%s/%s", externalApiUrl, "posts", postId, "comments")))
            .andRespond(withSuccess(
                "[\n"
                    + "        {\n"
                    + "            \"postId\": 100,\n"
                    + "            \"id\": 1,\n"
                    + "            \"name\": \"id labore ex et quam laborum\",\n"
                    + "            \"email\": \"Eliseo@gardner.biz\",\n"
                    + "            \"body\": \"laudantium enim quasi est quidem magnam voluptate ipsam eos\\ntempora quo necessitatibus\\ndolor quam autem quasi\\nreiciendis et nam sapiente accusantium\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"postId\": 100,\n"
                    + "            \"id\": 2,\n"
                    + "            \"name\": \"quo vero reiciendis velit similique earum\",\n"
                    + "            \"email\": \"Jayne_Kuhic@sydney.com\",\n"
                    + "            \"body\": \"est natus enim nihil est dolore omnis voluptatem numquam\\net omnis occaecati quod ullam at\\nvoluptatem error expedita pariatur\\nnihil sint nostrum voluptatem reiciendis et\"\n"
                    + "        }\n"
                    + "    ]",
                MediaType.APPLICATION_JSON));

        mockServer.expect(ExpectedCount.once(), requestTo(externalApiUrl + "/posts/100"))
            .andRespond(withSuccess(
                "{  \"userId\": 1,"
                    + "    \"id\": 100,"
                    + "    \"title\": \"title 1\","
                    + "    \"body\": \"body 1\""
                    + "  }", MediaType.APPLICATION_JSON));

        AuditionPost auditionPost = auditionIntegrationClient.getPostWithComments("100");
        mockServer.verify();

        assertEquals("title 1", auditionPost.getTitle());
        assertEquals(2, auditionPost.getComments().size());
        assertEquals(100, auditionPost.getComments().get(0).getPostId());
        assertEquals(100, auditionPost.getComments().get(1).getPostId());

        assertEquals(1, auditionPost.getComments().get(0).getId());
        assertEquals(2, auditionPost.getComments().get(1).getId());

        mockServer.reset();
    }

    /*
    Test the external API call for Get Post With Comments 404 HttpClientErrorException Scenario
`  */
    @Test
    public void testGetPostWithCommentsExceptionScenario404() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        String url = String.format("%s/%s/%s/%s", externalApiUrl, "posts", "101", "comments");
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        // Verify that the exception is correctly thrown
        AuditionIntegrationClient auditionIntegrationClient = new AuditionIntegrationClient(mockRestTemplate);
        auditionIntegrationClient.setExternalApiUrl(externalApiUrl);
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostWithComments("101"));
        assertEquals(HttpStatus.NOT_FOUND.value(), systemException.getStatusCode());
    }

    /*
    Test the external API call for Get Post With Comments Other than 404 HttpClientErrorException Scenario
`  */
    @Test
    public void testGetPostWithCommentsExceptionScenarioOtherThan404() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        String url = String.format("%s/%s/%s/%s", externalApiUrl, "posts", "101", "comments");
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        // Verify that the exception is correctly thrown
        AuditionIntegrationClient auditionIntegrationClient = new AuditionIntegrationClient(mockRestTemplate);
        auditionIntegrationClient.setExternalApiUrl(externalApiUrl);
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostWithComments("101"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), systemException.getStatusCode());
    }

    /*
    Test the external API call for Get Comments
     */
    @Test
    public void testGetPostComments() {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        String postId = "100";
        mockServer.expect(ExpectedCount.once(),
                requestTo(String.format("%s/%s/%s/%s", externalApiUrl, "posts", postId, "comments")))
            .andRespond(withSuccess(
                "[\n"
                    + "        {\n"
                    + "            \"postId\": 100,\n"
                    + "            \"id\": 1,\n"
                    + "            \"name\": \"id labore ex et quam laborum\",\n"
                    + "            \"email\": \"Eliseo@gardner.biz\",\n"
                    + "            \"body\": \"laudantium enim quasi est quidem magnam voluptate ipsam eos\\ntempora quo necessitatibus\\ndolor quam autem quasi\\nreiciendis et nam sapiente accusantium\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"postId\": 100,\n"
                    + "            \"id\": 2,\n"
                    + "            \"name\": \"quo vero reiciendis velit similique earum\",\n"
                    + "            \"email\": \"Jayne_Kuhic@sydney.com\",\n"
                    + "            \"body\": \"est natus enim nihil est dolore omnis voluptatem numquam\\net omnis occaecati quod ullam at\\nvoluptatem error expedita pariatur\\nnihil sint nostrum voluptatem reiciendis et\"\n"
                    + "        }\n"
                    + "    ]",
                MediaType.APPLICATION_JSON));

        List<Comment> comments = auditionIntegrationClient.getPostComments("100");
        mockServer.verify();

        assertEquals(2, comments.size());
        assertEquals(100, comments.get(0).getPostId());
        assertEquals(100, comments.get(1).getPostId());
        assertEquals(1, comments.get(0).getId());
        assertEquals(2, comments.get(1).getId());
        mockServer.reset();
    }

    /*
    Test the external API call for Get Comments 404 HttpClientErrorException Scenario
 `  */
    @Test
    public void testGetPostCommentsExceptionScenario404() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        String url = String.format("%s/%s/%s/%s", externalApiUrl, "posts", "101", "comments");
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        // Verify that the exception is correctly thrown
        AuditionIntegrationClient auditionIntegrationClient = new AuditionIntegrationClient(mockRestTemplate);
        auditionIntegrationClient.setExternalApiUrl(externalApiUrl);
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostComments("101"));
        assertEquals(HttpStatus.NOT_FOUND.value(), systemException.getStatusCode());

    }

    /*
Test the external API call for Get Comments Other than 404 HttpClientErrorException Scenario
`  */
    @Test
    public void testGetPostCommentsExceptionScenarioOther() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        String url = String.format("%s/%s/%s/%s", externalApiUrl, "posts", "101", "comments");
        when(mockRestTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Not Found"));

        // Verify that the exception is correctly thrown
        AuditionIntegrationClient auditionIntegrationClient = new AuditionIntegrationClient(mockRestTemplate);
        auditionIntegrationClient.setExternalApiUrl(externalApiUrl);
        SystemException systemException = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostComments("101"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), systemException.getStatusCode());
    }
}