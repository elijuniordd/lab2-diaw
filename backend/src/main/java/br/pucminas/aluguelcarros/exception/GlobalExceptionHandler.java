package br.pucminas.aluguelcarros.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<Map<String, Object>> conflito(ConflitoException e) {
        return erro(HttpStatus.CONFLICT, e.getMessage(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String, Object>> credenciais(BadCredentialsException e) {
        return erro(HttpStatus.UNAUTHORIZED, "Login ou senha inválidos", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));
        return erro(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String msg, Object detalhes) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", status.value());
        corpo.put("erro", msg);
        if (detalhes != null) {
            corpo.put("campos", detalhes);
        }
        return ResponseEntity.status(status).body(corpo);
    }
}
