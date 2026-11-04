package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Role;
import br.pucminas.aluguelcarros.model.Usuario;

public record UsuarioResponse(Long id, String login, Role role) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getLogin(), u.getRole());
    }
}
