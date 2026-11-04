package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Se {@code role} vier nulo, o usuário é criado como CLIENTE. */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 60) String login,
        @NotBlank @Size(min = 6, max = 100) String senha,
        Role role) {
}
