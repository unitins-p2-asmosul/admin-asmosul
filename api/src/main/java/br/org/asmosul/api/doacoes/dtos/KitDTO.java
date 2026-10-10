package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.Kit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class KitDTO {

    private KitDTO() {}

    public record ItemKitRequisicao(
        @NotNull(message = "O ID do item é obrigatório")
        Long idItem,
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade) {}

    public record ItemKitDetalhe(
        Long idItem,
        String nomeItem,
        Integer quantidade,
        Integer sobraEstoque) {}

    public record Requisicao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve conter no máximo 100 caracteres")
        String nome,
        String descricao,
        @NotEmpty(message = "O kit deve conter pelo menos um item")
        @Valid
        List<ItemKitRequisicao> itens) {

        public Kit paraEntidade() {
            return new Kit(this.nome, this.descricao);
        }
    }

    public record Atualizacao(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve conter no máximo 100 caracteres")
        String nome,
        String descricao,
        @NotEmpty(message = "O kit deve conter pelo menos um item")
        @Valid
        List<ItemKitRequisicao> itens) {}

    public record Resumo(
        Long id,
        String nome,
        List<String> itensNomes,
        Integer quantidadeDistribuivel) {

        public static Resumo deEntidade(Kit kit, List<String> itensNomes, Integer quantidadeDistribuivel) {
            return new Resumo(
                kit.getId(),
                kit.getNome(),
                itensNomes != null ? itensNomes : List.of(),
                quantidadeDistribuivel != null ? quantidadeDistribuivel : 0);
        }
    }

    public record Detalhe(
        Long id,
        String nome,
        String descricao,
        Integer quantidadeDistribuivel,
        List<ItemKitDetalhe> itens) {

        public static Detalhe deEntidade(Kit kit, Integer quantidadeDistribuivel, List<ItemKitDetalhe> itens) {
            return new Detalhe(
                kit.getId(),
                kit.getNome(),
                kit.getDescricao(),
                quantidadeDistribuivel != null ? quantidadeDistribuivel : 0,
                itens != null ? itens : List.of());
        }
    }

    public record KitFiltroDTO(
        String nome,
        List<Long> itensIds,
        Integer quantidadeDistribuivel) {}
}
