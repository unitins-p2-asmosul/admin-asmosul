
package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.Comorbidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ComorbidadeDTO {

    private ComorbidadeDTO() {}

    private static String normalizarNome(String nome) {
        return nome == null ? null : nome.trim().replaceAll("\\s+", " ");
    }

    public record Requisicao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
                    String nome,
            String descricao) {

        public Requisicao {
            nome = normalizarNome(nome);
        }

        public Comorbidade paraEntidade() {
            return new Comorbidade(this.nome, this.descricao);
        }
    }

    public record Atualizacao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
                    String nome,
            String descricao) {

        public Atualizacao {
            nome = normalizarNome(nome);
        }
    }

    public record Detalhe(Long id, String nome, String descricao) {
        public static Detalhe deEntidade(Comorbidade comorbidade) {
            return new Detalhe(
                    comorbidade.getId(),
                    comorbidade.getNome(),
                    comorbidade.getDescricao());
        }
    }

    public record Resumo(
            Long id, String nome, String descricao, Integer pessoasAfetadas, boolean ativo) {

        public static Resumo deEntidade(Comorbidade comorbidade) {
            return deEntidade(comorbidade, 0);
        }

        public static Resumo deEntidade(
                Comorbidade comorbidade, Integer pessoasAfetadas) {

            return new Resumo(
                    comorbidade.getId(),
                    comorbidade.getNome(),
                    comorbidade.getDescricao(),
                    pessoasAfetadas != null ? pessoasAfetadas : 0,
                    comorbidade.isAtivo());
        }
    }
}
