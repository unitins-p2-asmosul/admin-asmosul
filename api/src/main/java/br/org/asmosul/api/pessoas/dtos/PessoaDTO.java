package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class PessoaDTO {

    private PessoaDTO() {}

    /** DTO para criação de novos registros (POST /pessoas) */
    public record Requisicao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 255, message = "O nome deve conter no máximo 255 caracteres")
                    String nome,
            @NotBlank(message = "O CPF/CNPJ é obrigatório")
                    @Pattern(
                            regexp = "\\d{11}|\\d{14}",
                            message =
                                    "O CPF/CNPJ deve conter 11 (CPF) ou 14 (CNPJ) dígitos numéricos")
                    String cpfCnpj,
            TipoPessoa tipoPessoa,
            @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dataNascimento,
            Sexo sexo,
            @NotBlank(message = "O telefone é obrigatório")
                    @Pattern(
                            regexp = "\\d{10,11}",
                            message = "O telefone deve possuir 10 ou 11 dígitos numéricos")
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
            String descricao,
            @Size(max = 9, message = "O CEP deve conter no máximo 9 caracteres") String cep,
            Uf uf,
            @Size(max = 100, message = "A cidade deve conter no máximo 100 caracteres")
                    String cidade,
            @Size(max = 50, message = "O bairro deve conter no máximo 50 caracteres") String bairro,
            @Size(max = 100, message = "O logradouro deve conter no máximo 100 caracteres")
                    String logradouro,
            @Size(max = 100, message = "O complemento deve conter no máximo 100 caracteres")
                    String complementoEndereco,
            Integer quantidadeCoabitantes,
            Boolean ehBeneficiario,
            Boolean ehDoador) {
        public Pessoa paraEntidade() {
            return new Pessoa(
                    this.nome,
                    this.cpfCnpj,
                    this.tipoPessoa != null ? this.tipoPessoa : TipoPessoa.FISICA,
                    this.dataNascimento,
                    this.sexo,
                    this.telefone,
                    this.email,
                    this.escolaridade,
                    this.profissao,
                    this.rendaFamiliar,
                    this.descricao,
                    this.cep,
                    this.uf,
                    this.cidade,
                    this.bairro,
                    this.logradouro,
                    this.complementoEndereco,
                    this.quantidadeCoabitantes != null ? this.quantidadeCoabitantes : 0,
                    Boolean.TRUE.equals(this.ehBeneficiario),
                    Boolean.TRUE.equals(this.ehDoador));
        }
    }

    /** DTO para edição de registros existentes (PUT /pessoas/{id}) */
    public record Atualizacao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 255, message = "O nome deve conter no máximo 255 caracteres")
                    String nome,
            @NotBlank(message = "O CPF/CNPJ é obrigatório")
                    @Pattern(
                            regexp = "\\d{11}|\\d{14}",
                            message =
                                    "O CPF/CNPJ deve conter 11 (CPF) ou 14 (CNPJ) dígitos numéricos")
                    String cpfCnpj,
            TipoPessoa tipoPessoa,
            @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dataNascimento,
            Sexo sexo,
            @NotBlank(message = "O telefone é obrigatório")
                    @Pattern(
                            regexp = "\\d{10,11}",
                            message = "O telefone deve possuir 10 ou 11 dígitos numéricos")
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
            String descricao,
            @Size(max = 9, message = "O CEP deve conter no máximo 9 caracteres") String cep,
            Uf uf,
            @Size(max = 100, message = "A cidade deve conter no máximo 100 caracteres")
                    String cidade,
            @Size(max = 50, message = "O bairro deve conter no máximo 50 caracteres") String bairro,
            @Size(max = 100, message = "O logradouro deve conter no máximo 100 caracteres")
                    String logradouro,
            @Size(max = 100, message = "O complemento deve conter no máximo 100 caracteres")
                    String complementoEndereco,
            Integer quantidadeCoabitantes,
            Boolean ehBeneficiario,
            Boolean ehDoador) {}

    /** DTO para exibição resumida na listagem (US-18) */
    public record Resumo(
            Long id,
            String nome,
            String cpfCnpj,
            TipoPessoa tipoPessoa,
            @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dataNascimento,
            Sexo sexo,
            String telefone,
            String email,
            Escolaridade escolaridade,
            String profissao,
            String bairro,
            RendaFamiliar rendaFamiliar,
            List<ComorbidadeDTO.Resumo> comorbidades,
            List<CategoriaDTO.Resumo> categorias,
            Integer quantidadeCoabitantes,
            boolean ehBeneficiario,
            boolean ehDoador,
            boolean ativo) {
        public static Resumo deEntidade(Pessoa pessoa) {
            return new Resumo(
                    pessoa.getId(),
                    pessoa.getNome(),
                    pessoa.getCpfCnpj(),
                    pessoa.getTipoPessoa(),
                    pessoa.getDataNascimento(),
                    pessoa.getSexo(),
                    pessoa.getTelefone(),
                    pessoa.getEmail(),
                    pessoa.getEscolaridade(),
                    pessoa.getProfissao(),
                    pessoa.getBairro(),
                    pessoa.getRendaFamiliar(),
                    pessoa.getComorbidades() != null
                            ? pessoa.getComorbidades().stream()
                                    .map(ComorbidadeDTO.Resumo::deEntidade)
                                    .toList()
                            : List.of(),
                    pessoa.getCategorias() != null
                            ? pessoa.getCategorias().stream()
                                    .map(CategoriaDTO.Resumo::deEntidade)
                                    .toList()
                            : List.of(),
                    pessoa.getQuantidadeCoabitantes() != null
                            ? pessoa.getQuantidadeCoabitantes()
                            : 0,
                    pessoa.isEhBeneficiario(),
                    pessoa.isEhDoador(),
                    pessoa.isAtivo());
        }
    }

    /** DTO para exibição completa detalhada (US-25) */
    public record Detalhe(
            Long id,
            String nome,
            String cpfCnpj,
            TipoPessoa tipoPessoa,
            @JsonFormat(pattern = "dd-MM-yyyy") LocalDate dataNascimento,
            Sexo sexo,
            String telefone,
            String email,
            Escolaridade escolaridade,
            String profissao,
            RendaFamiliar rendaFamiliar,
            String cep,
            Uf uf,
            String cidade,
            String bairro,
            String logradouro,
            String complementoEndereco,
            Integer quantidadeCoabitantes,
            boolean ehBeneficiario,
            boolean ehDoador,
            List<ComorbidadeDTO.Resumo> comorbidades,
            List<CategoriaDTO.Resumo> categorias,
            String descricao,
            boolean ativo) {
        public static Detalhe deEntidade(Pessoa pessoa) {
            return new Detalhe(
                    pessoa.getId(),
                    pessoa.getNome(),
                    pessoa.getCpfCnpj(),
                    pessoa.getTipoPessoa(),
                    pessoa.getDataNascimento(),
                    pessoa.getSexo(),
                    pessoa.getTelefone(),
                    pessoa.getEmail(),
                    pessoa.getEscolaridade(),
                    pessoa.getProfissao(),
                    pessoa.getRendaFamiliar(),
                    pessoa.getCep(),
                    pessoa.getUf(),
                    pessoa.getCidade(),
                    pessoa.getBairro(),
                    pessoa.getLogradouro(),
                    pessoa.getComplementoEndereco(),
                    pessoa.getQuantidadeCoabitantes() != null
                            ? pessoa.getQuantidadeCoabitantes()
                            : 0,
                    pessoa.isEhBeneficiario(),
                    pessoa.isEhDoador(),
                    pessoa.getComorbidades() != null
                            ? pessoa.getComorbidades().stream()
                                    .map(ComorbidadeDTO.Resumo::deEntidade)
                                    .toList()
                            : List.of(),
                    pessoa.getCategorias() != null
                            ? pessoa.getCategorias().stream()
                                    .map(CategoriaDTO.Resumo::deEntidade)
                                    .toList()
                            : List.of(),
                    pessoa.getDescricao(),
                    pessoa.isAtivo());
        }
    }
}
