package br.pucminas.aluguelcarros.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    ResponseEntity<Map<String, Object>> acessoNegado(AcessoNegadoException e) {
        return erro(HttpStatus.FORBIDDEN, e.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> regraViolada(IllegalArgumentException e) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    /** Rede de segurança para corridas na unicidade (ex.: dois POST simultâneos com o mesmo CPF). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> integridade(DataIntegrityViolationException e) {
        return erro(HttpStatus.CONFLICT, "Conflito com dados já existentes", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> corpoIlegivel(HttpMessageNotReadableException e) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou ausente", null);
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
