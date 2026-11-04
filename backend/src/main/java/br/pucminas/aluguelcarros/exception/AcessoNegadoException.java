package br.pucminas.aluguelcarros.exception;

/** Usuário autenticado sem permissão sobre o recurso. Vira HTTP 403. */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
