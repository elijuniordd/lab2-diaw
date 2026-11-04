package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Parecer do agente: {@code aprovado=true} -> APROVADO; {@code false} -> REPROVADO. */
public record AvaliacaoRequest(
        @NotNull Boolean aprovado,
        @NotBlank @Size(max = 500) String parecer) {
}
