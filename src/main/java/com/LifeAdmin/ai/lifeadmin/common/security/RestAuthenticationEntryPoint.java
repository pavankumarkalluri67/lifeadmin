package com.LifeAdmin.ai.lifeadmin.common.security;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.LifeAdmin.ai.lifeadmin.common.error.ApiError;
import com.LifeAdmin.ai.lifeadmin.common.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Produces the standardized 401 response when a protected endpoint is reached
 * without a valid Access_Token (Req 6.1). Spring Security invokes this entry
 * point whenever the security context is unauthenticated; it writes the shared
 * {@link ApiError} body with code {@link ErrorCode#UNAUTHENTICATED} so the
 * shape matches every other error response (Req 29).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String MDC_TRACE_ID = "traceId";

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiError body = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .code(ErrorCode.UNAUTHENTICATED.name())
                .message("Authentication is required to access this resource.")
                .path(request.getRequestURI())
                .traceId(currentTraceId())
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }

    private static String currentTraceId() {
        String traceId = MDC.get(MDC_TRACE_ID);
        return (traceId == null || traceId.isBlank()) ? UUID.randomUUID().toString() : traceId;
    }
}
