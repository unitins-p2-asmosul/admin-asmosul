package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.Comorbidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ComorbidadeDTO {

    private ComorbidadeDTO() {}

    public record Requisicao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
                    String nome,
            String descricao) {
        public Comorbidade paraEntidade() {
            return new Comorbidade(this.nome, this.descricao);
        }
    }

    public record Atualizacao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
                    String nome,
            String descricao) {}

    public record Detalhe(Long id, String nome, String descricao) {
        public static Detalhe deEntidade(Comorbidade comorbidade) {
            return new Detalhe(
                    comorbidade.getId(), comorbidade.getNome(), comorbidade.getDescricao());
        }
    }

    public record Resumo(Long id, String nome, String descricao, boolean ativo) {
        public static Resumo deEntidade(Comorbidade comorbidade) {
            return new Resumo(
                    comorbidade.getId(),
                    comorbidade.getNome(),
                    comorbidade.getDescricao(),
                    comorbidade.isAtivo());
        }
    }
}
