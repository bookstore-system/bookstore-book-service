package com.notfound.bookservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookservice.model.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("!test")
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String GENERIC_ACCESS_DENIED_MESSAGE = "Access denied";

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        log.warn(
                "Access denied for {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                accessDeniedException.getMessage());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(
                response.getOutputStream(),
                ApiResponse.<Void>builder()
                        .code(403)
                        .message(GENERIC_ACCESS_DENIED_MESSAGE)
                        .data(null)
                        .build());
    }
}
