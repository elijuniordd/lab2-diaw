package br.pucminas.aluguelcarros.dto;

import java.time.LocalDate;

import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.StatusPedido;

/** O cliente vem completo (com rendimentos) para o agente fazer a análise financeira. */
public record PedidoResponse(Long id, LocalDate data, StatusPedido status, String parecerAgente,
        AutomovelResponse automovel, ClienteResponse cliente, String agente) {

    public static PedidoResponse de(Pedido p) {
        return new PedidoResponse(p.getId(), p.getData(), p.getStatus(), p.getParecerAgente(),
                AutomovelResponse.de(p.getAutomovel()), ClienteResponse.de(p.getCliente()),
                p.getAgente() != null ? p.getAgente().getLogin() : null);
    }
}
