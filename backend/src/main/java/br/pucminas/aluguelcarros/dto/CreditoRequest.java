package br.pucminas.aluguelcarros.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/** Contrato de crédito concedido por um banco. {@code juros} é % ao mês. */
public record CreditoRequest(
        @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
        @Digits(integer = 10, fraction = 2) BigDecimal valor,
        @NotNull @DecimalMin(value = "0.00") @Digits(integer = 4, fraction = 2) BigDecimal juros) {
}
