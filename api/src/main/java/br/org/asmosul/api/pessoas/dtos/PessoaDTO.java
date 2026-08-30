package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class PessoaDTO {

    private PessoaDTO() {}

    /**
     * DTO para criação de novos registros (POST /pessoas)
     */
    public record Requisicao(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter apenas números e 11 dígitos")
        String cpf,

        @NotNull(message = "A data de nascimento é obrigatória")
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dataNascimento,

        Sexo sexo,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve possuir 10 ou 11 dígitos numéricos")
        String telefone,

        @Email(message = "O e-mail informado deve ser válido")
        @Size(max = 50, message = "O e-mail deve conter no máximo 50 caracteres")
        String email,

        Escolaridade escolaridade,

        @Size(max = 50, message = "A profissão deve conter no máximo 50 caracteres")
        String profissao,

        RendaFamiliar rendaFamiliar,

        List<Long> comorbidades,
        List<Long> categorias,
        String descricao
    ) {
        public Pessoa paraEntidade() {
            return new Pessoa(
                this.nome,
                this.cpf,
                this.dataNascimento,
                this.sexo,
                this.telefone,
                this.email,
                this.escolaridade,
                this.profissao,
                this.rendaFamiliar,
                this.descricao
            );
        }
    }

    /**
     * DTO para edição de registros existentes (PUT /pessoas/{id})
     */
    public record Atualizacao(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter apenas números e 11 dígitos")
        String cpf,

        @NotNull(message = "A data de nascimento é obrigatória")
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dataNascimento,

        Sexo sexo,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve possuir 10 ou 11 dígitos numéricos")
        String telefone,

        @Email(message = "O e-mail informado deve ser válido")
        @Size(max = 50, message = "O e-mail deve conter no máximo 50 caracteres")
        String email,

        Escolaridade escolaridade,

        @Size(max = 50, message = "A profissão deve conter no máximo 50 caracteres")
        String profissao,

        RendaFamiliar rendaFamiliar,

        List<Long> comorbidades,
        List<Long> categorias,
        String descricao
    ) {}

    /**
     * DTO para exibição resumida na listagem
     */
    public record Resumo(
        Long id,
        String nome,
        String cpf,
        String telefone,
        String email,
        boolean ativo
    ) {
        public static Resumo deEntidade(Pessoa pessoa) {
            return new Resumo(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getTelefone(),
                pessoa.getEmail(),
                pessoa.isAtivo()
            );
        }
    }

    /**
     * DTO para exibição completa detalhada
     */
    public record Detalhe(
        Long id,
        String nome,
        String cpf,
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dataNascimento,
        Sexo sexo,
        String telefone,
        String email,
        Escolaridade escolaridade,
        String profissao,
        RendaFamiliar rendaFamiliar,
        List<Long> comorbidades,
        List<Long> categorias,
        String descricao,
        boolean ativo
    ) {
        public static Detalhe deEntidade(Pessoa pessoa) {
            return new Detalhe(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getSexo(),
                pessoa.getTelefone(),
                pessoa.getEmail(),
                pessoa.getEscolaridade(),
                pessoa.getProfissao(),
                pessoa.getRendaFamiliar(),
                pessoa.getComorbidades() != null
                    ? pessoa.getComorbidades().stream().map(Comorbidade::getId).toList()
                    : List.of(),
                pessoa.getCategorias() != null
                    ? pessoa.getCategorias().stream().map(Categoria::getId).toList()
                    : List.of(),
                pessoa.getDescricao(),
                pessoa.isAtivo()
            );
        }
    }
}
