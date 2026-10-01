package br.org.asmosul.api.acesso.dtos;

import br.org.asmosul.api.acesso.models.Conta;
import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.pessoas.models.Pessoa;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

public final class ContaDTO {

    private ContaDTO() {}

    /** DTO para criação de contas (POST /contas) - US-43 */
    public record Requisicao(
            @NotNull(message = "A pessoa vinculada é obrigatória") Long pessoaId,
            @NotBlank(message = "O nome de usuário é obrigatório")
                    @Size(max = 100, message = "O nome de usuário deve conter no máximo 100 caracteres")
                    String nomeUsuario,
            @NotBlank(message = "A senha temporária é obrigatória") String senhaTemporaria,
            @NotEmpty(message = "Informe ao menos um perfil de acesso")
                    Set<@NotNull(message = "O perfil informado não pode ser vazio") Perfil> perfis) {
        public Conta paraEntidade(Pessoa pessoa, String senhaHash) {
            return new Conta(pessoa, this.nomeUsuario, senhaHash, this.perfis);
        }

        @Override
        public String toString() {
            return "Requisicao[pessoaId=" + pessoaId
                    + ", nomeUsuario=" + nomeUsuario
                    + ", senhaTemporaria=***"
                    + ", perfis=" + perfis + "]";
        }
    }

    /** DTO para alteração do nome de usuário (PUT /contas/{id}) - RN019, US-58 */
    public record Atualizacao(
            @NotBlank(message = "O nome de usuário é obrigatório")
                    @Size(max = 100, message = "O nome de usuário deve conter no máximo 100 caracteres")
                    String nomeUsuario) {}

    /** DTO para atualização de perfis (PUT /contas/{id}/perfis) - RN01, US-44, US-45 */
    public record AtualizacaoPerfis(
            @NotEmpty(message = "Informe ao menos um perfil de acesso")
                    Set<@NotNull(message = "O perfil informado não pode ser vazio") Perfil> perfis) {}

    /** DTO para redefinição administrativa de senha (PATCH /contas/{id}/redefinir-senha) - US-59 */
    public record RedefinirSenhaAdmin(
            @NotBlank(message = "A nova senha temporária é obrigatória")
                    String novaSenhaTemporaria) {
        @Override
        public String toString() {
            return "RedefinirSenhaAdmin[novaSenhaTemporaria=***]";
        }
    }

    /** DTO para troca de senha pelo próprio usuário (PATCH /contas/minha-senha) - US-46 */
    public record RedefinirSenhaPropria(
            @NotBlank(message = "A senha atual é obrigatória") String senhaAtual,
            @NotBlank(message = "A nova senha é obrigatória") String novaSenha) {
        @Override
        public String toString() {
            return "RedefinirSenhaPropria[senhaAtual=***, novaSenha=***]";
        }
    }

    /** DTO para exibição resumida na listagem (US-50, US-55) */
    public record Resumo(
            Long id,
            String nomePessoa,
            String email,
            String nomeUsuario,
            Set<Perfil> perfis,
            boolean redefinirSenha,
            boolean ativo,
            @JsonFormat(pattern = "dd-MM-yyyy") LocalDateTime dataCriacao) {
        public static Resumo deEntidade(Conta conta) {
            return new Resumo(
                    conta.getId(),
                    conta.getPessoa().getNome(),
                    conta.getPessoa().getEmail(),
                    conta.getNomeUsuario(),
                    new TreeSet<>(conta.getPerfis()),
                    conta.isRedefinirSenha(),
                    conta.isAtivo(),
                    conta.getDataCriacao());
        }
    }

    /** DTO para exibição completa detalhada (US-54) - nunca expõe a senha ou o hash */
    public record Detalhe(
            Long id,
            Long pessoaId,
            String nomePessoa,
            String email,
            String nomeUsuario,
            Set<Perfil> perfis,
            boolean redefinirSenha,
            boolean ativo,
            @JsonFormat(pattern = "dd-MM-yyyy") LocalDateTime dataCriacao,
            @JsonFormat(pattern = "dd-MM-yyyy") LocalDateTime dataInativo) {
        public static Detalhe deEntidade(Conta conta) {
            return new Detalhe(
                    conta.getId(),
                    conta.getPessoa().getId(),
                    conta.getPessoa().getNome(),
                    conta.getPessoa().getEmail(),
                    conta.getNomeUsuario(),
                    new TreeSet<>(conta.getPerfis()),
                    conta.isRedefinirSenha(),
                    conta.isAtivo(),
                    conta.getDataCriacao(),
                    conta.getDataInativo());
        }
    }
}
