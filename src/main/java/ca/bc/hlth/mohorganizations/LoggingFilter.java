package ca.bc.hlth.mohorganizations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        int responseStatus = response.getStatus();
        LOGGER.info("{} : {}", request.getMethod(), request.getRequestURI());
        LOGGER.info("Completed : {}", responseStatus);

        if(isSuccessfulRequest(responseStatus)){
            filterChain.doFilter(request, response);
        } else {
            ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(request, cachedResponse);
            byte[] responseBody = cachedResponse.getContentAsByteArray();

            LOGGER.info("Response body : {}", new String(responseBody, StandardCharsets.UTF_8));

            cachedResponse.copyBodyToResponse();
        }
    }

    private boolean isSuccessfulRequest(int httpResponseStatus) {
        return 200 <= httpResponseStatus && httpResponseStatus <= 299;
    }
}
