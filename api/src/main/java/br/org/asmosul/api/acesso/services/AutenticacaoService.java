package br.org.asmosul.api.acesso.services;

import br.org.asmosul.api.acesso.dtos.AutenticacaoDTO;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import br.org.asmosul.api.comum.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService {

    private static final Logger log = LoggerFactory.getLogger(AutenticacaoService.class);

    private final AuthenticationManager authenticationManager;
    private final ContaRepository contaRepository;
    private final TokenService tokenService;

    public AutenticacaoService(
            AuthenticationManager authenticationManager,
            ContaRepository contaRepository,
            TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.contaRepository = contaRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public AutenticacaoDTO.LoginResposta autenticar(AutenticacaoDTO.LoginRequisicao requisicao) {
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            requisicao.nomeUsuario(), requisicao.senha()));
        } catch (AuthenticationException ex) {
            log.warn(
                    "Falha de login para usuário '{}': {}",
                    requisicao.nomeUsuario(),
                    ex.getMessage());
            throw ex;
        }

        var conta =
                contaRepository
                        .findByNomeUsuarioAndDataInativoIsNull(requisicao.nomeUsuario())
                        .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        var tokenGerado = tokenService.gerarToken(conta);
        log.info("Login realizado com sucesso para usuário '{}'", requisicao.nomeUsuario());

        return new AutenticacaoDTO.LoginResposta(
                tokenGerado.token(),
                "Bearer",
                tokenGerado.expiracao(),
                conta.getNomeUsuario(),
                conta.getPerfis());
    }
}
