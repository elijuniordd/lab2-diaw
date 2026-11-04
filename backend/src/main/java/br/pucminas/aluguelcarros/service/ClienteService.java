package br.pucminas.aluguelcarros.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.aluguelcarros.dto.ClienteRequest;
import br.pucminas.aluguelcarros.dto.ClienteResponse;
import br.pucminas.aluguelcarros.dto.RendimentoRequest;
import br.pucminas.aluguelcarros.exception.AcessoNegadoException;
import br.pucminas.aluguelcarros.exception.ConflitoException;
import br.pucminas.aluguelcarros.exception.RecursoNaoEncontradoException;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Rendimento;
import br.pucminas.aluguelcarros.model.Role;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;

/**
 * Regras de cliente:
 * <ul>
 *   <li>CPF único; no máximo 3 rendimentos;</li>
 *   <li>CLIENTE cria/lê/altera/remove apenas o próprio cadastro (um por usuário);</li>
 *   <li>AGENTE só consulta.</li>
 * </ul>
 */
@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;

    public ClienteService(ClienteRepository clientes, UsuarioRepository usuarios, PedidoRepository pedidos) {
        this.clientes = clientes;
        this.usuarios = usuarios;
        this.pedidos = pedidos;
    }

    public ClienteResponse criar(String login, ClienteRequest req) {
        Usuario usuario = usuarios.findByLogin(login).orElseThrow();
        if (usuario.getRole() != Role.CLIENTE) {
            throw new AcessoNegadoException("Apenas usuários com perfil CLIENTE possuem cadastro de cliente");
        }
        if (clientes.existsByUsuarioLogin(login)) {
            throw new ConflitoException("Este usuário já possui um cadastro de cliente");
        }
        String cpf = somenteDigitos(req.cpf());
        if (clientes.existsByCpf(cpf)) {
            throw new ConflitoException("CPF já cadastrado");
        }
        Cliente c = new Cliente();
        c.setUsuario(usuario);
        preencher(c, req, cpf);
        return ClienteResponse.de(clientes.save(c));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(String login, boolean agente) {
        if (agente) {
            return clientes.findAll().stream().map(ClienteResponse::de).toList();
        }
        return clientes.findByUsuarioLogin(login).map(ClienteResponse::de).stream().toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id, String login, boolean agente) {
        Cliente c = obter(id);
        if (!agente) {
            exigirDono(c, login);
        }
        return ClienteResponse.de(c);
    }

    public ClienteResponse atualizar(Long id, String login, boolean agente, ClienteRequest req) {
        Cliente c = obter(id);
        if (agente) {
            throw new AcessoNegadoException("Agentes não podem alterar clientes");
        }
        exigirDono(c, login);
        String cpf = somenteDigitos(req.cpf());
        if (clientes.existsByCpfAndIdNot(cpf, id)) {
            throw new ConflitoException("CPF já cadastrado");
        }
        preencher(c, req, cpf);
        return ClienteResponse.de(clientes.saveAndFlush(c));
    }

    public void remover(Long id, String login, boolean agente) {
        Cliente c = obter(id);
        if (agente) {
            throw new AcessoNegadoException("Agentes não podem remover clientes");
        }
        exigirDono(c, login);
        if (pedidos.existsByClienteId(id)) {
            throw new ConflitoException("Não é possível remover o cadastro: existem pedidos associados");
        }
        clientes.delete(c);
    }

    private void preencher(Cliente c, ClienteRequest req, String cpf) {
        c.setRg(req.rg().trim());
        c.setCpf(cpf);
        c.setNome(req.nome().trim());
        c.setEndereco(req.endereco().trim());
        c.setProfissao(req.profissao().trim());
        List<RendimentoRequest> recebidos = req.rendimentos() == null ? List.of() : req.rendimentos();
        c.definirRendimentos(recebidos.stream()
                .map(r -> new Rendimento(r.entidadeEmpregadora().trim(), r.valor()))
                .toList());
    }

    private Cliente obter(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    private void exigirDono(Cliente c, String login) {
        if (!c.getUsuario().getLogin().equals(login)) {
            throw new AcessoNegadoException("Você não tem permissão sobre este cliente");
        }
    }

    private String somenteDigitos(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}
