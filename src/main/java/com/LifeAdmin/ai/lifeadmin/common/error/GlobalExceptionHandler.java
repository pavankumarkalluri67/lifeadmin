package com.LifeAdmin.ai.lifeadmin.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Central exception handling that maps validation and domain exceptions to the
 * standardized {@link ApiError} body (Req 29.3). This is the single place error
 * responses are produced; controllers do not handle exceptions individually.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MDC_TRACE_ID = "traceId";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    /** Domain/business exceptions carrying an explicit {@link ErrorCode}. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        return build(ex.status(), ex.errorCode().name(), ex.getMessage(), request, List.of());
    }

    /** Bean Validation failure on {@code @RequestBody} / {@code @ModelAttribute} args (Req 29.2). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        List<ApiError.FieldError> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ApiError.FieldError(fieldError.getField(), messageOf(fieldError)));
        }
        for (ObjectError globalError : ex.getBindingResult().getGlobalErrors()) {
            errors.add(new ApiError.FieldError(globalError.getObjectName(), messageOf(globalError)));
        }
        return validationError(errors, request);
    }

    /** Bean Validation failure on method parameters (e.g. {@code @RequestParam}, path vars) (Req 29.2). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleHandlerMethodValidation(HandlerMethodValidationException ex,
                                                                  HttpServletRequest request) {
        List<ApiError.FieldError> errors = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> {
            String field = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error ->
                    errors.add(new ApiError.FieldError(field, error.getDefaultMessage())));
        });
        return validationError(errors, request);
    }

    /** Bean Validation failure surfaced as constraint violations (Req 29.2). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest request) {
        List<ApiError.FieldError> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(new ApiError.FieldError(lastNode(violation), violation.getMessage()));
        }
        return validationError(errors, request);
    }

    /** Optimistic locking conflict -> 409 STALE_UPDATE (Req 28.2). */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                         HttpServletRequest request) {
        return build(ErrorCode.STALE_UPDATE.defaultStatus(), ErrorCode.STALE_UPDATE.name(),
                "The resource was modified by another request. Please retry with the latest version.",
                request, List.of());
    }

    /** Upload exceeding the configured multipart size -> 413 FILE_TOO_LARGE. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex,
                                                        HttpServletRequest request) {
        return build(ErrorCode.FILE_TOO_LARGE.defaultStatus(), ErrorCode.FILE_TOO_LARGE.name(),
                "Uploaded file exceeds the maximum allowed size.", request, List.of());
    }

    /** Fallback for unexpected errors -> 500 with a generic code. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR,
                "An unexpected error occurred.", request, List.of());
    }

    private ResponseEntity<ApiError> validationError(List<ApiError.FieldError> errors, HttpServletRequest request) {
        return build(ErrorCode.VALIDATION_ERROR.defaultStatus(), ErrorCode.VALIDATION_ERROR.name(),
                "Request validation failed.", request, errors);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message,
                                           HttpServletRequest request, List<ApiError.FieldError> errors) {
        ApiError body = ApiError.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .path(request == null ? null : request.getRequestURI())
                .traceId(currentTraceId())
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private static String currentTraceId() {
        String traceId = MDC.get(MDC_TRACE_ID);
        return (traceId == null || traceId.isBlank()) ? UUID.randomUUID().toString() : traceId;
    }

    private static String messageOf(ObjectError error) {
        return error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage();
    }

    private static String lastNode(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString();
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }
}
