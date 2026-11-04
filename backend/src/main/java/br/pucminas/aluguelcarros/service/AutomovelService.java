package br.pucminas.aluguelcarros.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.aluguelcarros.dto.AutomovelRequest;
import br.pucminas.aluguelcarros.dto.AutomovelResponse;
import br.pucminas.aluguelcarros.exception.ConflitoException;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;

@Service
@Transactional
public class AutomovelService {

    private final AutomovelRepository automoveis;

    public AutomovelService(AutomovelRepository automoveis) {
        this.automoveis = automoveis;
    }

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar() {
        return automoveis.findAll().stream().map(AutomovelResponse::de).toList();
    }

    public AutomovelResponse criar(AutomovelRequest req) {
        String matricula = req.matricula().trim();
        String placa = req.placa().replace("-", "").toUpperCase();
        if (automoveis.existsByMatricula(matricula)) {
            throw new ConflitoException("Matrícula já cadastrada");
        }
        if (automoveis.existsByPlaca(placa)) {
            throw new ConflitoException("Placa já cadastrada");
        }
        Automovel a = new Automovel(null, matricula, req.ano(), req.marca().trim(), req.modelo().trim(), placa);
        return AutomovelResponse.de(automoveis.save(a));
    }
}
