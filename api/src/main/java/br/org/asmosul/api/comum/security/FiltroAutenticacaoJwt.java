package br.org.asmosul.api.comum.security;

import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final TokenService tokenService;
    private final ContaRepository contaRepository;

    public FiltroAutenticacaoJwt(TokenService tokenService, ContaRepository contaRepository) {
        this.tokenService = tokenService;
        this.contaRepository = contaRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extrairToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            tokenService
                    .validarEObterSubject(token)
                    .flatMap(contaRepository::findByNomeUsuarioAndDataInativoIsNull)
                    .ifPresent(
                            conta -> {
                                var authorities =
                                        conta.getPerfis().stream()
                                                .map(Perfil::name)
                                                .map(nome -> new SimpleGrantedAuthority("ROLE_" + nome))
                                                .toList();

                                var autenticacao =
                                        new UsernamePasswordAuthenticationToken(
                                                conta.getNomeUsuario(), null, authorities);
                                SecurityContextHolder.getContext().setAuthentication(autenticacao);
                            });
        }

        filterChain.doFilter(request, response);
    }

    private String extrairToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(PREFIXO_BEARER)) {
            return null;
        }

        String token = authorization.substring(PREFIXO_BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
