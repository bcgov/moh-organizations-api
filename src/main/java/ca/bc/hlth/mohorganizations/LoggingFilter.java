package ca.bc.hlth.mohorganizations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        LOGGER.info("{} : {}", request.getMethod(), request.getRequestURI());
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(request, cachedResponse);

        int responseStatus = cachedResponse.getStatus();


        if (!isSuccessfulRequest(responseStatus)) {
            byte[] responseBody = cachedResponse.getContentAsByteArray();
            LOGGER.warn("Completed : {}", responseStatus);
            if(responseBody.length > 0){
                LOGGER.warn("Response body : {}", new String(responseBody, StandardCharsets.UTF_8));
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
