package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoriaDTO {

    private CategoriaDTO() {}

    public record Requisicao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
                    String nome,
            String descricao) {
        public Categoria paraEntidade() {
            return new Categoria(this.nome, this.descricao);
        }
    }

    public record Atualizacao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
                    String nome,
            String descricao) {}

    public record Detalhe(Long id, String nome, String descricao) {
        public static Detalhe deEntidade(Categoria categoria) {
            return new Detalhe(categoria.getId(), categoria.getNome(), categoria.getDescricao());
        }
    }

    public record Resumo(Long id, String nome, String descricao, boolean ativo) {
        public static Resumo deEntidade(Categoria categoria) {
            return new Resumo(
                    categoria.getId(),
                    categoria.getNome(),
                    categoria.getDescricao(),
                    categoria.isAtivo());
        }
    }
}
