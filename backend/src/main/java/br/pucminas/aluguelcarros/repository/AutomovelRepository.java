package br.pucminas.aluguelcarros.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.aluguelcarros.model.Automovel;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    boolean existsByMatricula(String matricula);

    boolean existsByPlaca(String placa);
}
