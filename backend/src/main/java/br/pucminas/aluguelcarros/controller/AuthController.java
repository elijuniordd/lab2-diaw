package br.pucminas.aluguelcarros.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.aluguelcarros.dto.LoginRequest;
import br.pucminas.aluguelcarros.dto.RegisterRequest;
import br.pucminas.aluguelcarros.dto.TokenResponse;
import br.pucminas.aluguelcarros.dto.UsuarioResponse;
import br.pucminas.aluguelcarros.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.registrar(req);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    /** Rota protegida de exemplo: devolve o usuário dono do token. */
    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return auth.buscar(authentication.getName());
    }
}
