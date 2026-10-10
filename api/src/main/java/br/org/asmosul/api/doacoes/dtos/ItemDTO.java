package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class ItemDTO {

    private ItemDTO() {}

    public record Requisicao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve conter no máximo 100 caracteres")
        String nome,
        @NotNull(message = "A categoria é obrigatória")
        Long idCategoria,
        @NotNull(message = "O preço unitário é obrigatório")
        @PositiveOrZero(message = "O preço unitário deve ser maior ou igual a zero")
        Integer precoUnitario,
        @NotNull(message = "A unidade de medida é obrigatória")
        UnidadeMedida unidadeMedida,
        String descricao) {

        public Item paraEntidade(CategoriaItem categoria) {
            return new Item(
                this.nome,
                categoria,
                this.precoUnitario,
                this.unidadeMedida,
                this.descricao);
        }
    }

    public record Atualizacao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve conter no máximo 100 caracteres")
        String nome,
        @NotNull(message = "A categoria é obrigatória")
        Long idCategoria,
        @NotNull(message = "O preço unitário é obrigatório")
        @PositiveOrZero(message = "O preço unitário deve ser maior ou igual a zero")
        Integer precoUnitario,
        @NotNull(message = "A unidade de medida é obrigatória")
        UnidadeMedida unidadeMedida,
        String descricao) {}

    public record Resumo(
        Long id,
        String nome,
        CategoriaItemDTO.Resumo categoria,
        Integer estoque,
        Integer precoUnitario,
        UnidadeMedida unidadeMedida,
        String descricao,
        boolean ativo) {

        public static Resumo deEntidade(Item item, Integer estoqueCalculado) {
            return new Resumo(
                item.getId(),
                item.getNome(),
                item.getCategoria() != null ? CategoriaItemDTO.Resumo.deEntidade(item.getCategoria()) : null,
                estoqueCalculado != null ? estoqueCalculado : 0,
                item.getPrecoUnitario(),
                item.getUnidadeMedida(),
                item.getDescricao(),
                item.isAtivo());
        }
    }

    public record Detalhe(
        Long id,
        String nome,
        CategoriaItemDTO.Detalhe categoria,
        Integer estoque,
        Integer precoUnitario,
        UnidadeMedida unidadeMedida,
        String descricao,
        boolean ativo,
        LocalDateTime dataInativo) {

        public static Detalhe deEntidade(Item item, Integer estoqueCalculado) {
            return new Detalhe(
                item.getId(),
                item.getNome(),
                item.getCategoria() != null ? CategoriaItemDTO.Detalhe.deEntidade(item.getCategoria()) : null,
                estoqueCalculado != null ? estoqueCalculado : 0,
                item.getPrecoUnitario(),
                item.getUnidadeMedida(),
                item.getDescricao(),
                item.isAtivo(),
                item.getDataInativo());
        }

    }

    public record Filtro(
        String nome,
        List<Long> categoriasIds,
        Integer estoque,
        Integer precoUnitarioMinimo,
        Integer precoUnitarioMaximo,
        Boolean apenasInativos) {}
}
