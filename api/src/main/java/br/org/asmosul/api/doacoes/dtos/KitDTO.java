package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.Kit;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class KitDTO {

    private KitDTO() {}

    public static String normalizarNome(String nome) {
        return nome == null ? null : nome.trim().replaceAll("\\s+", " ");
    }

    public record ItemKitRequisicao(
            @NotNull(message = "O ID do item é obrigatório") Long idItem,
            @NotNull(message = "A quantidade é obrigatória")
                    @Positive(message = "A quantidade deve ser maior que zero")
                    Integer quantidade) {}

    public record ItemKitDetalhe(
            Long idItem,
            String nomeItem,
            UnidadeMedida unidadeMedida,
            Integer quantidade,
            Integer sobraEstoque) {

        public ItemKitDetalhe(
                Long idItem, String nomeItem, Integer quantidade, Integer sobraEstoque) {
            this(idItem, nomeItem, null, quantidade, sobraEstoque);
        }
    }

    public record Requisicao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 100, message = "O nome deve conter no máximo 100 caracteres")
                    String nome,
            String descricao,
            @NotEmpty(message = "O kit deve conter pelo menos um item") @Valid
                    List<ItemKitRequisicao> itens) {

        public Requisicao {
            nome = normalizarNome(nome);
        }

        public Kit paraEntidade() {
            return new Kit(this.nome, this.descricao);
        }
    }

    public record Atualizacao(
            @NotBlank(message = "O nome é obrigatório")
                    @Size(max = 100, message = "O nome deve conter no máximo 100 caracteres")
                    String nome,
            String descricao,
            @NotEmpty(message = "O kit deve conter pelo menos um item") @Valid
                    List<ItemKitRequisicao> itens) {

        public Atualizacao {
            nome = normalizarNome(nome);
        }
    }

    public record Resumo(
            Long id,
            String nome,
            String descricao,
            Integer totalItens,
            List<String> itensNomes,
            Integer quantidadeDistribuivel,
            boolean ativo) {

        public static Resumo deEntidade(
                Kit kit, List<String> itensNomes, Integer quantidadeDistribuivel) {
            List<String> listaNomes = itensNomes != null ? itensNomes : List.of();
            return new Resumo(
                    kit.getId(),
                    kit.getNome(),
                    kit.getDescricao(),
                    listaNomes.size(),
                    listaNomes,
                    quantidadeDistribuivel != null ? quantidadeDistribuivel : 0,
                    kit.isAtivo());
        }
    }

    public record Detalhe(
            Long id,
            String nome,
            String descricao,
            Integer totalItens,
            Integer quantidadeDistribuivel,
            List<ItemKitDetalhe> itens,
            boolean ativo,
            LocalDateTime dataInativo) {

        public static Detalhe deEntidade(
                Kit kit, Integer quantidadeDistribuivel, List<ItemKitDetalhe> itens) {
            List<ItemKitDetalhe> listaItens = itens != null ? itens : List.of();
            return new Detalhe(
                    kit.getId(),
                    kit.getNome(),
                    kit.getDescricao(),
                    listaItens.size(),
                    quantidadeDistribuivel != null ? quantidadeDistribuivel : 0,
                    listaItens,
                    kit.isAtivo(),
                    kit.getDataInativo());
        }
    }

    public record ItemDisponibilidade(
            Long idItem,
            String nomeItem,
            UnidadeMedida unidadeMedida,
            Integer quantidadePorKit,
            Integer saldoEstoque,
            Integer kitsPossiveisItem) {}

    public record Disponibilidade(
            Long idKit,
            String nomeKit,
            Long localArmazenamentoId,
            String nomeLocalArmazenamento,
            Integer quantidadeDisponivel,
            List<ItemDisponibilidade> itens) {}

    public record KitFiltroDTO(
            String nome,
            String descricao,
            List<Long> itensIds,
            Integer quantidadeDistribuivel,
            Boolean apenasInativos,
            Boolean incluirInativos) {}
}
