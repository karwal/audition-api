package com.audition.configuration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ResponseHeaderInjectorTest {

    @Autowired
    ResponseHeaderInjector responseHeaderInjector;

    @Test
    void testDoFilterInternal() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        try {
            doNothing().when(filterChain).doFilter(request, response);
            responseHeaderInjector.doFilterInternal(request, response, filterChain);
            verify(response, times(1)).addHeader(eq("X-Trace-Id"), anyString());
            verify(response, times(1)).addHeader(eq("X-Span-Id"), anyString());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}