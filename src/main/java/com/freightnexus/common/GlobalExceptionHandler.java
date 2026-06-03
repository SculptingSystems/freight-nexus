package com.freightnexus.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(int status, String error, String message, String path, Instant timestamp) {}

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return new ErrorResponse(404, "Not Found", ex.getMessage(), req.getRequestURI(), Instant.now());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return new ErrorResponse(403, "Forbidden", ex.getMessage(), req.getRequestURI(), Instant.now());
    }

    @ExceptionHandler(InsufficientCapacityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleCapacity(InsufficientCapacityException ex, HttpServletRequest req) {
        return new ErrorResponse(409, "Conflict", ex.getMessage(), req.getRequestURI(), Instant.now());
    }

    @ExceptionHandler(HOSViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleHOS(HOSViolationException ex, HttpServletRequest req) {
        return new ErrorResponse(409, "HOS Violation", ex.getMessage(), req.getRequestURI(), Instant.now());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return new ErrorResponse(422, "Unprocessable Entity", ex.getMessage(), req.getRequestURI(), Instant.now());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return new ErrorResponse(400, "Bad Request", ex.getMessage(), req.getRequestURI(), Instant.now());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ErrorResponse(400, "Bad Request", message, req.getRequestURI(), Instant.now());
    }
}
