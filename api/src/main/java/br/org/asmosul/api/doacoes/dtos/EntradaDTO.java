package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.Entrada;
import br.org.asmosul.api.doacoes.models.EntradaEstoque;
import br.org.asmosul.api.pessoas.models.Pessoa;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public final class EntradaDTO {

    private EntradaDTO() {}

    public record ItemEntradaRequisicao(
            @NotNull(message = "O ID do item é obrigatório") Long idItem,
            @NotNull(message = "O local de armazenamento é obrigatório") Long idArmazenamento,
            @NotNull(message = "A quantidade é obrigatória")
                    @Positive(message = "A quantidade deve ser maior que zero")
                    Integer quantidade) {}

    public record ItemEntradaResumo(Long idItem, String nomeItem, Integer quantidade) {

        public static ItemEntradaResumo deEntidade(EntradaEstoque entradaEstoque) {
            return new ItemEntradaResumo(
                    entradaEstoque.getItem().getId(),
                    entradaEstoque.getItem().getNome(),
                    entradaEstoque.getQuantidade());
        }
    }

    public record ItemEntradaDetalhe(
            Long id,
            Long idItem,
            String nomeItem,
            Long idArmazenamento,
            String nomeArmazenamento,
            Integer quantidade) {

        public static ItemEntradaDetalhe deEntidade(EntradaEstoque entradaEstoque) {
            return new ItemEntradaDetalhe(
                    entradaEstoque.getId(),
                    entradaEstoque.getItem().getId(),
                    entradaEstoque.getItem().getNome(),
                    entradaEstoque.getArmazenamento().getId(),
                    entradaEstoque.getArmazenamento().getNome(),
                    entradaEstoque.getQuantidade());
        }
    }

    public record Requisicao(
            @NotNull(message = "O doador é obrigatório") Long idDoador,
            String descricao,
            @NotEmpty(message = "A entrada deve conter ao menos um item")
                    @Valid
                    List<ItemEntradaRequisicao> itens) {

        public Entrada paraEntidade(Pessoa doador) {
            return new Entrada(doador, LocalDateTime.now(), this.descricao);
        }
    }

    /**
     * A atualização de entrada é propositalmente restrita a doador e descrição. Itens,
     * quantidades e endereços são alterados apenas pelos fluxos específicos de estoque.
     */
    public record Atualizacao(Long idDoador, String descricao) {}

    public record Resumo(
            Long id,
            List<ItemEntradaResumo> itens,
            Integer quantidadeTotalItens,
            String nomeDoador,
            @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime dataHora) {

        public static Resumo deEntidade(Entrada entrada, List<EntradaEstoque> itensEntrada) {
            List<ItemEntradaResumo> itens =
                    itensEntrada == null
                            ? List.of()
                            : itensEntrada.stream().map(ItemEntradaResumo::deEntidade).toList();

            int quantidadeTotal =
                    itensEntrada == null
                            ? 0
                            : itensEntrada.stream()
                                    .mapToInt(
                                            item ->
                                                    item.getQuantidade() != null
                                                            ? item.getQuantidade()
                                                            : 0)
                                    .sum();

            return new Resumo(
                    entrada.getId(),
                    itens,
                    quantidadeTotal,
                    entrada.getDoador() != null ? entrada.getDoador().getNome() : null,
                    entrada.getDataHora());
        }
    }

    public record Detalhe(
            Long id,
            Long idDoador,
            String nomeDoador,
            @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime dataHora,
            String descricao,
            List<ItemEntradaDetalhe> itens) {

        public static Detalhe deEntidade(Entrada entrada, List<ItemEntradaDetalhe> itens) {
            return new Detalhe(
                    entrada.getId(),
                    entrada.getDoador() != null ? entrada.getDoador().getId() : null,
                    entrada.getDoador() != null ? entrada.getDoador().getNome() : null,
                    entrada.getDataHora(),
                    entrada.getDescricao(),
                    itens != null ? itens : List.of());
        }
    }

    public record EntradaFiltroDTO(
            Long itemId,
            Integer quantidade,
            Long doadorId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            Long enderecoId) {}
}
