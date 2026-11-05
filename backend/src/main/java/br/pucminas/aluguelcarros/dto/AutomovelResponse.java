package br.pucminas.aluguelcarros.dto;

import br.pucminas.aluguelcarros.model.Automovel;

public record AutomovelResponse(Long id, String matricula, Integer ano, String marca, String modelo, String placa,
        ProprietarioResponse proprietario) {

    public static AutomovelResponse de(Automovel a) {
        return new AutomovelResponse(a.getId(), a.getMatricula(), a.getAno(), a.getMarca(), a.getModelo(),
                a.getPlaca(), ProprietarioResponse.de(a.getProprietario()));
    }
}
