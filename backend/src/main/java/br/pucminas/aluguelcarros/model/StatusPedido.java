package br.pucminas.aluguelcarros.model;

/**
 * Ciclo de vida do pedido:
 * PENDENTE -> EM_ANALISE -> APROVADO | REPROVADO; APROVADO -> CONTRATADO;
 * o cliente pode CANCELAR antes de CONTRATADO.
 */
public enum StatusPedido {
    PENDENTE,
    EM_ANALISE,
    APROVADO,
    REPROVADO,
    CANCELADO,
    CONTRATADO
}
