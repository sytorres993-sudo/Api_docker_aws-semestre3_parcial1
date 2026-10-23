package com.example.gimnasio_sara.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ErrorResponse(Instant timestamp, int status, String mensaje, String ruta, Map<String, String> errores) {}
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request, Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), message, request.getRequestURI(), fields));
    }
    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorResponse> notFound(RecursoNoEncontradoException ex, HttpServletRequest r) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), r, Map.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest r) {
        Map<String, String> fields = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Revisa los campos enviados", r, fields);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception ex, HttpServletRequest r) {
        return error(HttpStatus.BAD_REQUEST, "JSON o parámetros inválidos. Revisa los tipos y el nivel de la rutina", r, Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> conflict(DataIntegrityViolationException ex, HttpServletRequest r) {
        return error(HttpStatus.CONFLICT, "Los datos entran en conflicto con otro cambio. Actualiza e intenta de nuevo", r, Map.of());
    }
}
