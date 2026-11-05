package br.pucminas.aluguelcarros.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.TipoContrato;

public record ContratoResponse(Long id, Long pedidoId, TipoContrato tipo, LocalDate dataInicio, LocalDate dataFim,
        BigDecimal valor, AutomovelResponse automovel, String cliente, CreditoResponse credito) {

    public static ContratoResponse de(Contrato c) {
        return new ContratoResponse(c.getId(), c.getPedido().getId(), c.getTipo(), c.getDataInicio(), c.getDataFim(),
                c.getValor(), AutomovelResponse.de(c.getPedido().getAutomovel()), c.getPedido().getCliente().getNome(),
                CreditoResponse.de(c.getCredito()));
    }
}
