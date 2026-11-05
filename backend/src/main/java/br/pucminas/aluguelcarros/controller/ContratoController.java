package br.pucminas.aluguelcarros.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.aluguelcarros.dto.ContratoRequest;
import br.pucminas.aluguelcarros.dto.ContratoResponse;
import br.pucminas.aluguelcarros.dto.CreditoRequest;
import br.pucminas.aluguelcarros.service.ContratoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/contratos")
public class ContratoController {

    private final ContratoService service;

    public ContratoController(ContratoService service) {
        this.service = service;
    }

    /** CLIENTE executa o contrato de um pedido APROVADO. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse criar(Authentication auth, @Valid @RequestBody ContratoRequest req) {
        return service.criar(auth.getName(), ehAgente(auth), req);
    }

    /** CLIENTE: apenas os seus contratos. AGENTE: todos. */
    @GetMapping
    public List<ContratoResponse> listar(Authentication auth) {
        return service.listar(auth.getName(), ehAgente(auth));
    }

    @GetMapping("/{id}")
    public ContratoResponse buscar(Authentication auth, @PathVariable Long id) {
        return service.buscar(id, auth.getName(), ehAgente(auth));
    }

    /** BANCO associa um contrato de crédito ao contrato de aluguel. */
    @PostMapping("/{id}/credito")
    @PreAuthorize("hasRole('BANCO')")
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse associarCredito(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody CreditoRequest req) {
        return service.associarCredito(id, auth.getName(), req);
    }

    private boolean ehAgente(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENTE"));
    }
}
