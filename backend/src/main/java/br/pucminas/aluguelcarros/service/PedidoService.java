package br.pucminas.aluguelcarros.service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.aluguelcarros.dto.AvaliacaoRequest;
import br.pucminas.aluguelcarros.dto.PedidoRequest;
import br.pucminas.aluguelcarros.dto.PedidoResponse;
import br.pucminas.aluguelcarros.exception.AcessoNegadoException;
import br.pucminas.aluguelcarros.exception.ConflitoException;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Pedido;
import br.pucminas.aluguelcarros.model.StatusPedido;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;

/**
 * Regras de pedido:
 * <ul>
 *   <li>CLIENTE cria (nasce PENDENTE), consulta os seus, modifica enquanto PENDENTE e cancela até ser CONTRATADO;</li>
 *   <li>AGENTE lista todos, avalia (APROVADO/REPROVADO com parecer) e modifica (volta para EM_ANALISE).</li>
 * </ul>
 */
@Service
@Transactional
public class PedidoService {

    private static final Set<StatusPedido> AVALIAVEIS = EnumSet.of(StatusPedido.PENDENTE, StatusPedido.EM_ANALISE);
    private static final Set<StatusPedido> CANCELAVEIS =
            EnumSet.of(StatusPedido.PENDENTE, StatusPedido.EM_ANALISE, StatusPedido.APROVADO);

    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final AutomovelRepository automoveis;
    private final UsuarioRepository usuarios;

    public PedidoService(PedidoRepository pedidos, ClienteRepository clientes,
            AutomovelRepository automoveis, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.automoveis = automoveis;
        this.usuarios = usuarios;
    }

    public PedidoResponse criar(String login, boolean agente, PedidoRequest req) {
        if (agente) {
            throw new AcessoNegadoException("Apenas clientes podem introduzir pedidos");
        }
        Cliente cliente = clientes.findByUsuarioLogin(login)
                .orElseThrow(() -> new ConflitoException("Preencha seu cadastro de cliente antes de criar um pedido"));
        Pedido p = new Pedido();
        p.setCliente(cliente);
        p.setAutomovel(obterAutomovel(req.automovelId()));
        p.setData(LocalDate.now());
        p.setStatus(StatusPedido.PENDENTE);
        return PedidoResponse.de(pedidos.save(p));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(String login, boolean agente) {
        List<Pedido> lista = agente ? pedidos.findAllByOrderByIdDesc()
                : pedidos.findByClienteUsuarioLoginOrderByIdDesc(login);
        return lista.stream().map(PedidoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id, String login, boolean agente) {
        Pedido p = obter(id);
        if (!agente) {
            exigirDono(p, login);
        }
        return PedidoResponse.de(p);
    }

    public PedidoResponse modificar(Long id, String login, boolean agente, PedidoRequest req) {
        Pedido p = obter(id);
        if (agente) {
            if (!AVALIAVEIS.contains(p.getStatus())) {
                throw new ConflitoException("Só é possível modificar pedidos PENDENTE ou EM_ANALISE");
            }
            p.setAutomovel(obterAutomovel(req.automovelId()));
            p.setStatus(StatusPedido.EM_ANALISE);
            p.setAgente(usuarios.findByLogin(login).orElseThrow());
        } else {
            exigirDono(p, login);
            if (p.getStatus() != StatusPedido.PENDENTE) {
                throw new ConflitoException("Só é possível modificar pedidos PENDENTE");
            }
            p.setAutomovel(obterAutomovel(req.automovelId()));
        }
        return PedidoResponse.de(pedidos.saveAndFlush(p));
    }

    public PedidoResponse cancelar(Long id, String login, boolean agente) {
        Pedido p = obter(id);
        if (agente) {
            throw new AcessoNegadoException("Apenas o cliente pode cancelar o próprio pedido");
        }
        exigirDono(p, login);
        if (!CANCELAVEIS.contains(p.getStatus())) {
            throw new ConflitoException("Pedido com status " + p.getStatus() + " não pode ser cancelado");
        }
        p.setStatus(StatusPedido.CANCELADO);
        return PedidoResponse.de(pedidos.saveAndFlush(p));
    }

    public PedidoResponse avaliar(Long id, String login, boolean agente, AvaliacaoRequest req) {
        if (!agente) {
            throw new AcessoNegadoException("Apenas agentes podem avaliar pedidos");
        }
        Pedido p = obter(id);
        if (!AVALIAVEIS.contains(p.getStatus())) {
            throw new ConflitoException("Só é possível avaliar pedidos PENDENTE ou EM_ANALISE");
        }
        p.setStatus(req.aprovado() ? StatusPedido.APROVADO : StatusPedido.REPROVADO);
        p.setParecerAgente(req.parecer().trim());
        p.setAgente(usuarios.findByLogin(login).orElseThrow());
        return PedidoResponse.de(pedidos.saveAndFlush(p));
    }

    private Pedido obter(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado"));
    }

    private Automovel obterAutomovel(Long id) {
        return automoveis.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Automóvel não encontrado"));
    }

    private void exigirDono(Pedido p, String login) {
        if (!p.getCliente().getUsuario().getLogin().equals(login)) {
            throw new AcessoNegadoException("Você não tem permissão sobre este pedido");
        }
    }
}
