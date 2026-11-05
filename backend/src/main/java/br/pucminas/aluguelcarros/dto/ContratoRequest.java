package br.pucminas.aluguelcarros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/** Confirmação do cliente: executa o contrato de um pedido APROVADO. */
public record ContratoRequest(
        @NotNull Long pedidoId,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2) BigDecimal valor) {
}
