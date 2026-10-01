package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.CategoriaDoacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class CategoriaDoacaoDTO {

    private CategoriaDoacaoDTO() {}

    public record Requisicao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
                    String nome,
            String descricao) {
        public CategoriaDoacao paraEntidade() {
            return new CategoriaDoacao(this.nome, this.descricao);
        }
    }

    public record Atualizacao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
                    String nome,
            String descricao) {}

    public record Resumo(Long id, String nome, String descricao, boolean ativo) {
        public static Resumo deEntidade(CategoriaDoacao categoria) {
            return new Resumo(
                    categoria.getId(),
                    categoria.getNome(),
                    categoria.getDescricao(),
                    categoria.isAtivo());
        }
    }

    public record Detalhe(
            Long id,
            String nome,
            String descricao,
            boolean ativo,
            LocalDateTime dataInativo) {
        public static Detalhe deEntidade(CategoriaDoacao categoria) {
            return new Detalhe(
                    categoria.getId(),
                    categoria.getNome(),
                    categoria.getDescricao(),
                    categoria.isAtivo(),
                    categoria.getDataInativo());
        }
    }
}
