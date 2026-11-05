package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Usuario;

/** {@code tipo}: CLIENTE, EMPRESA ou BANCO. */
public record ProprietarioResponse(String login, String tipo) {

    public static ProprietarioResponse de(Usuario u) {
        return u == null ? null : new ProprietarioResponse(u.getLogin(), u.perfil());
    }
}
