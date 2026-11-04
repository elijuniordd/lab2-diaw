package br.pucminas.aluguelcarros.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RendimentoRequest(
        @NotBlank @Size(max = 255) String entidadeEmpregadora,
        @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "deve ser maior que zero") BigDecimal valor) {
}
