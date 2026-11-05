package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Role;
import br.pucminas.aluguelcarros.model.TipoAgente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Se {@code role} vier nulo, o usuário é criado como CLIENTE. Para AGENTE, {@code tipoAgente}
 * (EMPRESA ou BANCO) assume EMPRESA quando omitido; é ignorado para CLIENTE.
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 60) String login,
        @NotBlank @Size(min = 6, max = 100) String senha,
        Role role,
        TipoAgente tipoAgente) {
}
