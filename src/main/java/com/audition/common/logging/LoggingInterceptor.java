package com.audition.common.logging;

import com.audition.web.advice.ExceptionControllerAdvice;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditionLogger logger;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
        throws IOException {
        // Log the request details
        logRequest(request, body);

        // Execute the request and get the response
        ClientHttpResponse response = execution.execute(request, body);

        // Log the response details
        logResponse(response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        try {
            String bodyContent = new String(body, StandardCharsets.UTF_8);
            String formattedBody = !bodyContent.isEmpty() ? formatJsonIfApplicable(bodyContent) : "(empty body)";

            logger.info(LOG, String.format("Request: Method: %s URL: %s Headers: %s Body: %s ",
                request.getMethod().name(), request.getURI(), request.getHeaders().entrySet(),
                formattedBody));
        } catch (Exception e) {
            logger.warn(LOG, String.format("Failed to log request body: %s", e));
        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        String formattedBody = responseBody.length() > 0 ? formatJsonIfApplicable(responseBody) : "(empty body)";

        logger.info(LOG, String.format("Response: Status: {} Headers: {} Body: {}",
            response.getStatusCode(), response.getHeaders(), formattedBody));
    }

    private String formatJsonIfApplicable(String content) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(content));
        } catch (Exception e) {
            return content; // Return as-is if not a valid JSON
        }
    }
}

