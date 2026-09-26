package br.org.asmosul.api.acesso.services;

import br.org.asmosul.api.acesso.dtos.AutenticacaoDTO;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import br.org.asmosul.api.comum.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService {

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
        authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        requisicao.nomeUsuario(), requisicao.senha()));

        var conta =
                contaRepository
                        .findByNomeUsuarioAndDataInativoIsNull(requisicao.nomeUsuario())
                        .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        var tokenGerado = tokenService.gerarToken(conta);

        return new AutenticacaoDTO.LoginResposta(
                tokenGerado.token(),
                "Bearer",
                tokenGerado.expiracao(),
                conta.getNomeUsuario(),
                conta.getPerfis());
    }
}
