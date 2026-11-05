package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Role;
import br.pucminas.aluguelcarros.model.TipoAgente;

public record TokenResponse(String token, String tipo, long expiraEmSegundos, String login, Role role,
        TipoAgente tipoAgente) {
}
