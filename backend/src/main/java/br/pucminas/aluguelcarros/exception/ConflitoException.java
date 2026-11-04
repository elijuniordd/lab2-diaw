package br.pucminas.aluguelcarros.exception;

/** Violação de regra de unicidade (ex.: login já existente). Vira HTTP 409. */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
