package br.pucminas.aluguelcarros.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.aluguelcarros.dto.LoginRequest;
import br.pucminas.aluguelcarros.dto.RegisterRequest;
import br.pucminas.aluguelcarros.dto.TokenResponse;
import br.pucminas.aluguelcarros.dto.UsuarioResponse;
import br.pucminas.aluguelcarros.exception.ConflitoException;
import br.pucminas.aluguelcarros.model.Role;
import br.pucminas.aluguelcarros.model.Usuario;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import br.pucminas.aluguelcarros.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder,
            AuthenticationManager authManager, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @Transactional
    public UsuarioResponse registrar(RegisterRequest req) {
        if (usuarios.existsByLogin(req.login())) {
            throw new ConflitoException("Login já está em uso");
        }
        Role role = req.role() != null ? req.role() : Role.CLIENTE;
        Usuario novo = new Usuario(null, req.login(), encoder.encode(req.senha()), role);
        return UsuarioResponse.de(usuarios.save(novo));
    }

    public TokenResponse login(LoginRequest req) {
        // Lança BadCredentialsException (-> 401) se login/senha estiverem errados.
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.login(), req.senha()));
        Usuario u = usuarios.findByLogin(req.login()).orElseThrow();
        return new TokenResponse(jwt.gerar(u), "Bearer", jwt.expiraEmSegundos(), u.getLogin(), u.getRole());
    }

    public UsuarioResponse buscar(String login) {
        return usuarios.findByLogin(login).map(UsuarioResponse::de).orElseThrow();
    }
}
