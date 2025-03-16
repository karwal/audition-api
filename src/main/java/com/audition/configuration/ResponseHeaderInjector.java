package com.audition.configuration;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Getter
@Setter
public class ResponseHeaderInjector extends OncePerRequestFilter {

    // TODO-Resolved Inject openTelemetry trace and span Ids in the response headers.
    private final Tracer tracer;

    public ResponseHeaderInjector(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            response.addHeader("X-Trace-Id", currentSpan.context().traceId());
            response.addHeader("X-Span-Id", currentSpan.context().spanId());
        }
        filterChain.doFilter(request, response);
    }
}
