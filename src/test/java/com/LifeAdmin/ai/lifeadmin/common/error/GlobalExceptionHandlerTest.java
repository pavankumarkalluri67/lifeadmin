package com.LifeAdmin.ai.lifeadmin.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlobalExceptionHandler} exception-to-{@link ApiError}
 * mapping (Req 29.1, 29.2). These are plain unit tests: the handler methods are
 * invoked directly with constructed exceptions and a mocked request, so no
 * database or Spring context is required.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private HttpServletRequest request;

    private HttpServletRequest requestFor(String uri) {
        lenient().when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

    /**
     * ApiException maps to its ErrorCode's default status and code name, and the
     * body carries the standardized fields (Req 29.1, 29.3).
     */
    @Test
    void apiExceptionMapsToErrorCodeStatusAndCode() {
        ApiException ex = new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "not here");

        ResponseEntity<ApiError> response = handler.handleApiException(ex, requestFor("/api/things/1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(body.code()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(body.message()).isEqualTo("not here");
        assertThat(body.path()).isEqualTo("/api/things/1");
        assertThat(body.timestamp()).isNotNull();
        assertThat(body.traceId()).isNotBlank();
        // Non-validation errors carry an empty errors array.
        assertThat(body.errors()).isEmpty();
    }

    /**
     * A different ErrorCode surfaces its own default status, confirming the
     * mapping is driven by the code rather than hardcoded (Req 29.3).
     */
    @Test
    void apiExceptionUsesErrorCodeDefaultStatusForConflict() {
        ApiException ex = new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS, "dup");

        ResponseEntity<ApiError> response = handler.handleApiException(ex, requestFor("/api/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("EMAIL_ALREADY_EXISTS");
    }

    /**
     * MethodArgumentNotValidException maps to VALIDATION_ERROR (400) with one
     * errors[] entry per invalid field, each containing field and message
     * (Req 29.2).
     */
    @Test
    void methodArgumentNotValidMapsToValidationErrorWithFieldErrors() {
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "createRequest");
        bindingResult.addError(new org.springframework.validation.FieldError(
                "createRequest", "email", "must not be blank"));
        bindingResult.addError(new org.springframework.validation.FieldError(
                "createRequest", "password", "size must be between 8 and 64"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.handleMethodArgumentNotValid(ex, requestFor("/api/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.errors()).hasSize(2);
        assertThat(body.errors())
                .extracting(ApiError.FieldError::field, ApiError.FieldError::message)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("email", "must not be blank"),
                        org.assertj.core.groups.Tuple.tuple("password", "size must be between 8 and 64"));
    }

    /**
     * Every entry in the VALIDATION_ERROR errors[] array has both a field and a
     * message populated (Req 29.2 shape).
     */
    @Test
    void validationErrorsArrayEntriesHaveFieldAndMessage() {
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "req");
        bindingResult.addError(new org.springframework.validation.FieldError(
                "req", "name", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.handleMethodArgumentNotValid(ex, requestFor("/api/x"));

        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.errors()).allSatisfy(fieldError -> {
            assertThat(fieldError.field()).isNotBlank();
            assertThat(fieldError.message()).isNotBlank();
        });
    }

    /**
     * ConstraintViolationException (e.g. on @RequestParam / path vars) maps to
     * VALIDATION_ERROR with the leaf property name as the field (Req 29.2).
     */
    @Test
    void constraintViolationMapsToValidationErrorWithLeafField() {
        ConstraintViolation<?> violation = mockViolation("createUser.email", "must be a valid email");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ApiError> response = handler.handleConstraintViolation(ex, requestFor("/api/users"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.errors()).hasSize(1);
        assertThat(body.errors().get(0).field()).isEqualTo("email");
        assertThat(body.errors().get(0).message()).isEqualTo("must be a valid email");
    }

    /** Optimistic locking failure maps to 409 STALE_UPDATE (Req 28.2). */
    @Test
    void optimisticLockingMapsToStaleUpdate() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("stale");

        ResponseEntity<ApiError> response = handler.handleOptimisticLock(ex, requestFor("/api/things/1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("STALE_UPDATE");
        assertThat(response.getBody().errors()).isEmpty();
    }

    /** Oversized multipart upload maps to 413 FILE_TOO_LARGE. */
    @Test
    void maxUploadSizeMapsToFileTooLarge() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1_048_576L);

        ResponseEntity<ApiError> response = handler.handleMaxUploadSize(ex, requestFor("/api/documents"));

        assertThat(response.getStatusCode().value()).isEqualTo(413);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FILE_TOO_LARGE");
    }

    /** Any unexpected exception maps to a generic 500 INTERNAL_ERROR. */
    @Test
    void unexpectedExceptionMapsToInternalError() {
        Exception ex = new IllegalStateException("boom");

        ResponseEntity<ApiError> response = handler.handleUnexpected(ex, requestFor("/api/things"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.status()).isEqualTo(500);
        // The generic message must not leak internal exception details.
        assertThat(body.message()).doesNotContain("boom");
    }

    /** A null request must not break error construction; path is simply null. */
    @Test
    void nullRequestProducesNullPathWithoutError() {
        ApiException ex = new ApiException(ErrorCode.UNAUTHENTICATED, "nope");

        ResponseEntity<ApiError> response = handler.handleApiException(ex, null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path()).isNull();
        assertThat(response.getBody().code()).isEqualTo("UNAUTHENTICATED");
    }

    private static ConstraintViolation<?> mockViolation(String propertyPath, String message) {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
}
