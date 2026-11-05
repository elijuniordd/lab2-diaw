package br.pucminas.aluguelcarros.dto;

import java.math.BigDecimal;

import br.pucminas.aluguelcarros.model.ContratoCredito;

public record CreditoResponse(Long id, String banco, BigDecimal valor, BigDecimal juros) {

    public static CreditoResponse de(ContratoCredito c) {
        return c == null ? null : new CreditoResponse(c.getId(), c.getBanco().getLogin(), c.getValor(), c.getJuros());
    }
}
