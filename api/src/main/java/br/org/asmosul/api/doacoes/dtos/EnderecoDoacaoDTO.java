package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.EnderecoDoacao;
import br.org.asmosul.api.pessoas.models.Uf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class EnderecoDoacaoDTO {

    private EnderecoDoacaoDTO() {}

    public record Requisicao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 100, message = "O nome deve conter no máximo 100 caracteres")
                    String nome,
            @NotBlank(message = "O CEP é obrigatório")
                    @Size(max = 9, message = "O CEP deve conter no máximo 9 caracteres")
                    String cep,
            @NotNull(message = "O estado (UF) é obrigatório") Uf uf,
            @NotBlank(message = "A cidade é obrigatória")
                    @Size(max = 100, message = "A cidade deve conter no máximo 100 caracteres")
                    String cidade,
            @NotBlank(message = "O bairro é obrigatório")
                    @Size(max = 50, message = "O bairro deve conter no máximo 50 caracteres")
                    String bairro,
            @NotBlank(message = "O logradouro é obrigatório")
                    @Size(max = 100, message = "O logradouro deve conter no máximo 100 caracteres")
                    String logradouro,
            @NotBlank(message = "O número é obrigatório")
                    @Size(max = 20, message = "O número deve conter no máximo 20 caracteres")
                    String numero,
            @Size(max = 100, message = "O complemento deve conter no máximo 100 caracteres")
                    String complemento,
            String informacoesAdicionais) {

        public EnderecoDoacao paraEntidade() {
            return new EnderecoDoacao(
                    this.nome,
                    this.cep,
                    this.uf,
                    this.cidade,
                    this.bairro,
                    this.logradouro,
                    this.numero,
                    this.complemento,
                    this.informacoesAdicionais);
        }
    }

    public record Atualizacao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 100, message = "O nome deve conter no máximo 100 caracteres")
                    String nome,
            @NotBlank(message = "O CEP é obrigatório")
                    @Size(max = 9, message = "O CEP deve conter no máximo 9 caracteres")
                    String cep,
            @NotNull(message = "O estado (UF) é obrigatório") Uf uf,
            @NotBlank(message = "A cidade é obrigatória")
                    @Size(max = 100, message = "A cidade deve conter no máximo 100 caracteres")
                    String cidade,
            @NotBlank(message = "O bairro é obrigatório")
                    @Size(max = 50, message = "O bairro deve conter no máximo 50 caracteres")
                    String bairro,
            @NotBlank(message = "O logradouro é obrigatório")
                    @Size(max = 100, message = "O logradouro deve conter no máximo 100 caracteres")
                    String logradouro,
            @NotBlank(message = "O número é obrigatório")
                    @Size(max = 20, message = "O número deve conter no máximo 20 caracteres")
                    String numero,
            @Size(max = 100, message = "O complemento deve conter no máximo 100 caracteres")
                    String complemento,
            String informacoesAdicionais) {}

    public record Resumo(
            Long id,
            String nome,
            String cep,
            Uf uf,
            String cidade,
            String bairro,
            String logradouro,
            String numero,
            String complemento,
            boolean ativo) {

        public static Resumo deEntidade(EnderecoDoacao endereco) {
            return new Resumo(
                    endereco.getId(),
                    endereco.getNome(),
                    endereco.getCep(),
                    endereco.getUf(),
                    endereco.getCidade(),
                    endereco.getBairro(),
                    endereco.getLogradouro(),
                    endereco.getNumero(),
                    endereco.getComplemento(),
                    endereco.isAtivo());
        }
    }

    public record Detalhe(
            Long id,
            String nome,
            String cep,
            Uf uf,
            String cidade,
            String bairro,
            String logradouro,
            String numero,
            String complemento,
            String informacoesAdicionais,
            boolean ativo,
            LocalDateTime dataInativo) {

        public static Detalhe deEntidade(EnderecoDoacao endereco) {
            return new Detalhe(
                    endereco.getId(),
                    endereco.getNome(),
                    endereco.getCep(),
                    endereco.getUf(),
                    endereco.getCidade(),
                    endereco.getBairro(),
                    endereco.getLogradouro(),
                    endereco.getNumero(),
                    endereco.getComplemento(),
                    endereco.getInformacoesAdicionais(),
                    endereco.isAtivo(),
                    endereco.getDataInativo());
        }
    }
}
