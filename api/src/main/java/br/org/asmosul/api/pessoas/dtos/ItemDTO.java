package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.models.Item;
import br.org.asmosul.api.pessoas.models.UnidadeMedida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class ItemDTO {

    private ItemDTO() {}

    /** DTO para criação de novos itens (POST /itens) */
    @Schema(name = "ItemRequisicao")
    public record Requisicao(
            @NotBlank(message = "O nome do item é obrigatório")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
            String nome,

            @NotNull(message = "A categoria é obrigatória")
            Long categoriaId,

            @NotNull(message = "O preço unitário é obrigatório")
            @PositiveOrZero(message = "O preço unitário deve ser maior ou igual a zero")
            Double precoUnitario,

            @NotNull(message = "A unidade de medida é obrigatória")
            UnidadeMedida unidadeMedida,

            String descricao
    ) {
        public Item paraEntidade(Categoria categoria) {
            return new Item(this.nome, categoria, this.precoUnitario, this.unidadeMedida, this.descricao);
        }
    }

    /** DTO para atualização de itens (PUT /itens/{id}) */
    @Schema(name = "ItemAtualizacao")
    public record Atualizacao(
            @NotBlank(message = "O nome é obrigatório")
            @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
            String nome,

            @NotNull(message = "A categoria é obrigatória")
            Long categoriaId,

            @NotNull(message = "O preço unitário é obrigatório")
            @PositiveOrZero(message = "O preço unitário deve ser maior ou igual a zero")
            Double precoUnitario,

            @NotNull(message = "A unidade de medida é obrigatória")
            UnidadeMedida unidadeMedida,

            String descricao
    ) {}

    /** DTO para exibição resumida na listagem */
    @Schema(name = "ItemResumo")
    public record Resumo(
            Long id,
            String nome,
            String categoriaNome,
            Double estoque,
            Double precoUnitario,
            UnidadeMedida unidadeMedida,
            String descricao,
            boolean ativo
    ) {
        public static Resumo deEntidade(Item item, Double estoqueCalculado) {
            return new Resumo(
                    item.getId(),
                    item.getNome(),
                    item.getCategoria() != null ? item.getCategoria().getNome() : null,
                    estoqueCalculado != null ? estoqueCalculado : 0.0,
                    item.getPrecoUnitario(),
                    item.getUnidadeMedida(),
                    item.getDescricao(),
                    item.isAtivo()
            );
        }
    }

    /** DTO para exibição detalhada */
    @Schema(name = "ItemDetalhe")
    public record Detalhe(
            Long id,
            String nome,
            CategoriaDTO.Resumo categoria,
            Double estoque,
            Double precoUnitario,
            UnidadeMedida unidadeMedida,
            String descricao,
            boolean ativo
    ) {
        public static Detalhe deEntidade(Item item, Double estoqueCalculado) {
            return new Detalhe(
                    item.getId(),
                    item.getNome(),
                    item.getCategoria() != null ? CategoriaDTO.Resumo.deEntidade(item.getCategoria()) : null,
                    estoqueCalculado != null ? estoqueCalculado : 0.0,
                    item.getPrecoUnitario(),
                    item.getUnidadeMedida(),
                    item.getDescricao(),
                    item.isAtivo()
            );
        }
    }
}