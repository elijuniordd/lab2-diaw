package br.pucminas.aluguelcarros.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AutomovelRequest(
        @NotBlank @Size(max = 30) String matricula,
        @NotNull @Min(1950) @Max(2100) Integer ano,
        @NotBlank @Size(max = 60) String marca,
        @NotBlank @Size(max = 60) String modelo,
        @NotBlank @Pattern(regexp = "(?i)[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}",
                message = "placa inválida (ex.: ABC1D23 ou ABC-1234)") String placa) {
}
