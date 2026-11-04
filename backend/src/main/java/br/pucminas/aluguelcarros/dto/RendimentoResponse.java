package br.pucminas.aluguelcarros.dto;

import java.math.BigDecimal;

import br.pucminas.aluguelcarros.model.Rendimento;

public record RendimentoResponse(Long id, String entidadeEmpregadora, BigDecimal valor) {

    public static RendimentoResponse de(Rendimento r) {
        return new RendimentoResponse(r.getId(), r.getEntidadeEmpregadora(), r.getValor());
    }
}
