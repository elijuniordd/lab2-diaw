package br.pucminas.aluguelcarros.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.aluguelcarros.dto.AvaliacaoRequest;
import br.pucminas.aluguelcarros.dto.PedidoRequest;
import br.pucminas.aluguelcarros.dto.PedidoResponse;
import br.pucminas.aluguelcarros.service.PedidoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(Authentication auth, @Valid @RequestBody PedidoRequest req) {
        return service.criar(auth.getName(), ehAgente(auth), req);
    }

    /** CLIENTE: apenas os seus pedidos. AGENTE: todos. */
    @GetMapping
    public List<PedidoResponse> listar(Authentication auth) {
        return service.listar(auth.getName(), ehAgente(auth));
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(Authentication auth, @PathVariable Long id) {
        return service.buscar(id, auth.getName(), ehAgente(auth));
    }

    @PutMapping("/{id}")
    public PedidoResponse modificar(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody PedidoRequest req) {
        return service.modificar(id, auth.getName(), ehAgente(auth), req);
    }

    @PutMapping("/{id}/cancelar")
    public PedidoResponse cancelar(Authentication auth, @PathVariable Long id) {
        return service.cancelar(id, auth.getName(), ehAgente(auth));
    }

    @PutMapping("/{id}/avaliar")
    public PedidoResponse avaliar(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody AvaliacaoRequest req) {
        return service.avaliar(id, auth.getName(), ehAgente(auth), req);
    }

    private boolean ehAgente(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENTE"));
    }
}
