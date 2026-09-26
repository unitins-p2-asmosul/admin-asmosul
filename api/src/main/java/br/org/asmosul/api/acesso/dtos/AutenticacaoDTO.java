package br.org.asmosul.api.acesso.dtos;

import br.org.asmosul.api.acesso.models.Perfil;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Set;

public final class AutenticacaoDTO {

    private AutenticacaoDTO() {}

    public record LoginRequisicao(
            @NotBlank(message = "O nome de usuário é obrigatório") String nomeUsuario,
            @NotBlank(message = "A senha é obrigatória") String senha) {}

    public record LoginResposta(
            String token,
            String tipo,
            Instant expiracao,
            String nomeUsuario,
            Set<Perfil> perfis) {}
}
