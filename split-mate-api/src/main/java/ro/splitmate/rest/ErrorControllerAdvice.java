package ro.splitmate.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import ro.splitmate.types.ApiError;
import ro.splitmate.types.ApiValidationError;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestControllerAdvice
class ErrorControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ApiError> wrongCode(AccessDeniedException e) {
        ApiError apiError = new ApiError(FORBIDDEN, e);
        return ResponseEntity.status(FORBIDDEN).body(apiError);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> error(WebExchangeBindException e) {
        ApiError apiError = new ApiError(BAD_REQUEST, e);
        apiError.setSubErrors(e.getFieldErrors()
                .stream()
                .map(fieldError -> new ApiValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList()));
        return ResponseEntity.badRequest().body(apiError);
    }
}
