package ca.bc.hlth.mohorganizations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isHealthCheck(request)) {
            filterChain.doFilter(request, response);
        } else {
            logRequestAndResponse(request, response, filterChain);
        }
    }

    private boolean isHealthCheck(HttpServletRequest request) {
        return request.getRequestURI().equals("/health");
    }

    private void logRequestAndResponse(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        LOGGER.info("{} : {}", request.getMethod(), request.getRequestURI());
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, cachedResponse);
        int responseStatus = cachedResponse.getStatus();

        if (!isSuccessfulRequest(responseStatus)) {
            byte[] responseBody = cachedResponse.getContentAsByteArray();
            LOGGER.error("Error : {}", responseStatus);
            if (responseBody.length > 0) {
                LOGGER.error("Response body : {}", new String(responseBody, StandardCharsets.UTF_8));
            }
        } else {
            LOGGER.info("Completed : {}", responseStatus);
        }
        cachedResponse.copyBodyToResponse();
    }

    private boolean isSuccessfulRequest(int httpResponseStatus) {
        return 200 <= httpResponseStatus && httpResponseStatus <= 299;
    }
}
