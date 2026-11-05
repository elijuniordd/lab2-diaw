package br.pucminas.aluguelcarros.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.pucminas.aluguelcarros.model.Usuario;

/** Emite tokens JWT (HS256) com o login no {@code sub} e o perfil na claim {@code roles}. */
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final Duration validade;

    public JwtService(JwtEncoder encoder,
            @Value("${app.jwt.expiration-minutes:120}") long minutos) {
        this.encoder = encoder;
        this.validade = Duration.ofMinutes(minutos);
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("aluguelcarros")
                .subject(usuario.getLogin())
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .claim("roles", roles(usuario))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /** Perfil (CLIENTE/AGENTE) e, para agentes, o tipo (EMPRESA/BANCO), todos viram {@code ROLE_*}. */
    private static List<String> roles(Usuario u) {
        List<String> roles = new ArrayList<>();
        roles.add(u.getRole().name());
        if (u.getTipoAgente() != null) {
            roles.add(u.getTipoAgente().name());
        }
        return roles;
    }

    public long expiraEmSegundos() {
        return validade.toSeconds();
    }
}
