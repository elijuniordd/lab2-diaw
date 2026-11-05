package br.pucminas.aluguelcarros.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.aluguelcarros.model.Contrato;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findAllByOrderByIdDesc();

    List<Contrato> findByPedidoClienteUsuarioLoginOrderByIdDesc(String login);

    boolean existsByPedidoId(Long pedidoId);
}
