
package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoriaDTO {

    private CategoriaDTO() {}

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

        public Categoria paraEntidade() {
            return new Categoria(this.nome, this.descricao);
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
        public static Detalhe deEntidade(Categoria categoria) {
            return new Detalhe(
                    categoria.getId(),
                    categoria.getNome(),
                    categoria.getDescricao());
        }
    }

    public record Resumo(
            Long id, String nome, String descricao, Integer pessoasPertencentes, boolean ativo) {

        public static Resumo deEntidade(Categoria categoria) {
            return deEntidade(categoria, 0);
        }

        public static Resumo deEntidade(
                Categoria categoria, Integer pessoasPertencentes) {

            return new Resumo(
                    categoria.getId(),
                    categoria.getNome(),
                    categoria.getDescricao(),
                    pessoasPertencentes != null ? pessoasPertencentes : 0,
                    categoria.isAtivo());
        }
    }
}
