package br.pucminas.aluguelcarros.dto;

import java.util.List;

import br.pucminas.aluguelcarros.model.Cliente;

public record ClienteResponse(Long id, String rg, String cpf, String nome, String endereco,
        String profissao, List<RendimentoResponse> rendimentos) {

    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.getId(), c.getRg(), c.getCpf(), c.getNome(), c.getEndereco(),
                c.getProfissao(), c.getRendimentos().stream().map(RendimentoResponse::de).toList());
    }
}
