package br.org.asmosul.api.comum.security;

import br.org.asmosul.api.acesso.models.Conta;
import br.org.asmosul.api.acesso.models.Perfil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private static final String ISSUER = "asmosul-api";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long expiracaoMinutos;

    public TokenService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiracao-minutos:120}") long expiracaoMinutos) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public TokenGerado gerarToken(Conta conta) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plus(expiracaoMinutos, ChronoUnit.MINUTES);
        String[] perfis = conta.getPerfis().stream().map(Perfil::name).sorted().toArray(String[]::new);

        String token =
                JWT.create()
                        .withIssuer(ISSUER)
                        .withSubject(conta.getNomeUsuario())
                        .withClaim("id", conta.getId())
                        .withArrayClaim("perfis", perfis)
                        .withIssuedAt(Date.from(agora))
                        .withExpiresAt(Date.from(expiracao))
                        .sign(algorithm);

        return new TokenGerado(token, expiracao);
    }

    public Optional<String> validarEObterSubject(String token) {
        try {
            String subject = verifier.verify(token).getSubject();
            return Optional.ofNullable(subject);
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }

    public record TokenGerado(String token, Instant expiracao) {}
}
