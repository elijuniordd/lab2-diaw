package br.pucminas.aluguelcarros.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.aluguelcarros.dto.ContratoRequest;
import br.pucminas.aluguelcarros.dto.ContratoResponse;
import br.pucminas.aluguelcarros.dto.CreditoRequest;
import br.pucminas.aluguelcarros.exception.AcessoNegadoException;
import br.pucminas.aluguelcarros.exception.ConflitoException;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.model.TipoContrato;
import br.pucminas.aluguelcarros.repository.ContratoRepository;
import br.pucminas.aluguelcarros.repository.PedidoRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;

/**
 * Regras de contrato:
 * <ul>
 *   <li>o CLIENTE dono de um pedido APROVADO o executa: nasce o contrato e o pedido vira CONTRATADO;</li>
 *   <li>um BANCO associa um contrato de crédito (valor e juros) a um contrato existente, uma única vez.</li>
 * </ul>
 */
@Service
@Transactional
public class ContratoService {

    private final ContratoRepository contratos;
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;

    public ContratoService(ContratoRepository contratos, PedidoRepository pedidos, UsuarioRepository usuarios) {
        this.contratos = contratos;
        this.pedidos = pedidos;
        this.usuarios = usuarios;
    }

    public ContratoResponse criar(String login, boolean agente, ContratoRequest req) {
        if (agente) {
            throw new AcessoNegadoException("Apenas o cliente pode executar o contrato do próprio pedido");
        }
        Pedido p = pedidos.findById(req.pedidoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
        if (!p.getCliente().getUsuario().getLogin().equals(login)) {
            throw new AcessoNegadoException("Você não tem permissão sobre este pedido");
        }
        if (p.getStatus() != StatusPedido.APROVADO || contratos.existsByPedidoId(p.getId())) {
            throw new ConflitoException("Só é possível contratar pedidos APROVADO ainda sem contrato");
        }
        if (!req.dataFim().isAfter(req.dataInicio())) {
            throw new ConflitoException("A data final deve ser posterior à data inicial");
        }
        Contrato c = new Contrato();
        c.setPedido(p);
        c.setTipo(TipoContrato.ALUGUEL);
        c.setDataInicio(req.dataInicio());
        c.setDataFim(req.dataFim());
        c.setValor(req.valor());
        p.setStatus(StatusPedido.CONTRATADO);
        pedidos.save(p);
        return ContratoResponse.de(contratos.saveAndFlush(c));
    }

    public ContratoResponse associarCredito(Long id, String loginBanco, CreditoRequest req) {
        Contrato c = obter(id);
        if (c.getCredito() != null) {
            throw new ConflitoException("Este contrato já possui contrato de crédito");
        }
        ContratoCredito credito = new ContratoCredito();
        credito.setBanco(usuarios.findByLogin(loginBanco).orElseThrow());
        credito.setValor(req.valor());
        credito.setJuros(req.juros());
        c.associarCredito(credito);
        return ContratoResponse.de(contratos.saveAndFlush(c));
    }

    @Transactional(readOnly = true)
    public List<ContratoResponse> listar(String login, boolean agente) {
        List<Contrato> lista = agente ? contratos.findAllByOrderByIdDesc()
                : contratos.findByPedidoClienteUsuarioLoginOrderByIdDesc(login);
        return lista.stream().map(ContratoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ContratoResponse buscar(Long id, String login, boolean agente) {
        Contrato c = obter(id);
        if (!agente && !c.getPedido().getCliente().getUsuario().getLogin().equals(login)) {
            throw new AcessoNegadoException("Você não tem permissão sobre este contrato");
        }
        return ContratoResponse.de(c);
    }

    private Contrato obter(Long id) {
        return contratos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Contrato não encontrado"));
    }
}
