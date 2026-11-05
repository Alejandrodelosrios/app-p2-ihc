package com.ihc.mascotas.backend.shared.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ihc.mascotas.backend.shared.api.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", fields);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError handleBadCredentials(BadCredentialsException exception) {
        return error(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos", Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiError handleConflict(ConflictException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleInvalidCurrentPassword(InvalidCurrentPasswordException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleInvalidResetToken(InvalidResetTokenException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError handleNotFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiError handleIntegrityViolation(DataIntegrityViolationException exception) {
        return error(HttpStatus.CONFLICT, "El recurso ya existe o entra en conflicto con datos existentes", Map.of());
    }

    @ExceptionHandler(NotAllowedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleNotAllowed(NotAllowedException exception){
        return error(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            Map.of()
        );
    }

    private static ApiError error(HttpStatus status, String message, Map<String, String> fields) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, fields);
    }
}
