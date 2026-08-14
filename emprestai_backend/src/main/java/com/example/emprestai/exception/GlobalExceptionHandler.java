package com.example.emprestai.exception;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduz erros de autenticação, instalação e validação em respostas HTTP consistentes. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<Map<String, Object>> invalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", 401, "message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidSetupKeyException.class)
    ResponseEntity<Map<String, Object>> invalidSetupKey(InvalidSetupKeyException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("status", 403, "message", exception.getMessage()));
    }

    @ExceptionHandler(InitialSetupUnavailableException.class)
    ResponseEntity<Map<String, Object>> initialSetupUnavailable(InitialSetupUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("status", 409, "message", exception.getMessage()));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    ResponseEntity<Map<String, Object>> emailJaCadastrado(EmailJaCadastradoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("status", 409, "message", exception.getMessage()));
    }

    @ExceptionHandler(UnidadeEscolarNotFoundException.class)
    ResponseEntity<Map<String, Object>> unidadeEscolarNaoEncontrada(UnidadeEscolarNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", 404, "message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(Map.of("status", 400, "message", "Validação falhou", "details", details));
    }
}
