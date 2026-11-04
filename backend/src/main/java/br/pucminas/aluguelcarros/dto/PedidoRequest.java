package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.NotNull;

/** Entrada de POST /pedidos e PUT /pedidos/{id}. */
public record PedidoRequest(@NotNull Long automovelId) {
}
