package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.Saida;
import br.org.asmosul.api.doacoes.models.SaidaEstoque;
import br.org.asmosul.api.pessoas.models.Pessoa;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class SaidaDTO {

    private SaidaDTO() {}

    public record ItemSaidaRequisicao(
        @NotNull(message = "O ID do item é obrigatório")
        Long idItem,
        @NotNull(message = "O local de origem do estoque é obrigatório")
        Long idArmazenamento,
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade) {}

    public record KitSaidaRequisicao(
        @NotNull(message = "O ID do kit é obrigatório")
        Long idKit,
        @NotNull(message = "A quantidade de kits é obrigatória")
        @Positive(message = "A quantidade de kits deve ser maior que zero")
        Integer quantidade,
        @NotNull(message = "O local de saída do kit é obrigatório")
        Long idArmazenamento) {}

    public record ItemSaidaDetalhe(
        Long id,
        Long idItem,
        String nomeItem,
        Long idArmazenamento,
        String nomeArmazenamento,
        Integer quantidade) {

        public static ItemSaidaDetalhe deEntidade(SaidaEstoque saidaEstoque) {
            return new ItemSaidaDetalhe(
                saidaEstoque.getId(),
                saidaEstoque.getItem().getId(),
                saidaEstoque.getItem().getNome(),
                saidaEstoque.getArmazenamento().getId(),
                saidaEstoque.getArmazenamento().getNome(),
                saidaEstoque.getQuantidade());
        }
    }

    public record Requisicao(
        @NotNull(message = "O recebedor é obrigatório")
        Long idRecebedor,
        String descricao,
        @Valid
        List<ItemSaidaRequisicao> itens,
        @Valid
        List<KitSaidaRequisicao> kits) {

        public Saida paraEntidade(Pessoa recebedor) {
            return new Saida(recebedor, LocalDateTime.now(), this.descricao);
        }
    }

    public record Atualizacao(
        @NotNull(message = "O recebedor é obrigatório")
        Long idRecebedor,
        String descricao,
        @NotEmpty(message = "A saída deve conter itens ou kits")
        @Valid
        List<ItemSaidaRequisicao> itens) {}

    public record Resumo(
        Long id,
        String nomeRecebedor,
        List<String> itensOuKitsDescricao,
        Integer quantidadeTotal,
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dataHora) {

        public static Resumo deEntidade(
            Saida saida,
            List<String> itensOuKitsDescricao,
            Integer quantidadeTotal) {
            return new Resumo(
                saida.getId(),
                saida.getRecebedor() != null ? saida.getRecebedor().getNome() : null,
                itensOuKitsDescricao != null ? itensOuKitsDescricao : List.of(),
                quantidadeTotal != null ? quantidadeTotal : 0,
                saida.getDataHora());
        }
    }

    public record Detalhe(
        Long id,
        Long idRecebedor,
        String nomeRecebedor,
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dataHora,
        String descricao,
        List<ItemSaidaDetalhe> itens) {

        public static Detalhe deEntidade(Saida saida, List<ItemSaidaDetalhe> itens) {
            return new Detalhe(
                saida.getId(),
                saida.getRecebedor() != null ? saida.getRecebedor().getId() : null,
                saida.getRecebedor() != null ? saida.getRecebedor().getNome() : null,
                saida.getDataHora(),
                saida.getDescricao(),
                itens != null ? itens : List.of());
        }
    }
    public record SaidaFiltroDTO(
        List<Long> itensIds,
        List<Long> kitsIds,
        Integer quantidade,
        Long recebedorId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate data) {}
}
