package br.pucminas.aluguelcarros.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.aluguelcarros.dto.ClienteRequest;
import br.pucminas.aluguelcarros.dto.ClienteResponse;
import br.pucminas.aluguelcarros.service.ClienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(Authentication auth, @Valid @RequestBody ClienteRequest req) {
        return service.criar(auth.getName(), req);
    }

    @GetMapping
    public List<ClienteResponse> listar(Authentication auth) {
        return service.listar(auth.getName(), ehAgente(auth));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(Authentication auth, @PathVariable Long id) {
        return service.buscar(id, auth.getName(), ehAgente(auth));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody ClienteRequest req) {
        return service.atualizar(id, auth.getName(), ehAgente(auth), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(Authentication auth, @PathVariable Long id) {
        service.remover(id, auth.getName(), ehAgente(auth));
    }

    private boolean ehAgente(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENTE"));
    }
}
