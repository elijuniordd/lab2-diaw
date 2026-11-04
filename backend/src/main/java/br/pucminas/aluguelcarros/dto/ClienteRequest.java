package br.pucminas.aluguelcarros.dto;

import java.util.List;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Entrada de POST/PUT /clientes. O CPF pode vir com ou sem máscara. */
public record ClienteRequest(
        @NotBlank @Size(max = 20) String rg,
        @NotBlank @CPF String cpf,
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Size(max = 255) String endereco,
        @NotBlank @Size(max = 100) String profissao,
        @Valid @Size(max = 3, message = "no máximo 3 rendimentos") List<RendimentoRequest> rendimentos) {
}
