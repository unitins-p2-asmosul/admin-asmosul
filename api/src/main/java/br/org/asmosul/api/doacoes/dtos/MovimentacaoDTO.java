package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Movimentacao;
import br.org.asmosul.api.doacoes.models.MovimentacaoEstoque;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.Locale;

public final class MovimentacaoDTO {

    private MovimentacaoDTO() {}

    public record ItemMovimentacaoRequisicao(
        @NotNull(message = "O ID do item é obrigatório")
        Long idItem,
        @NotNull(message = "O local de origem antigo é obrigatório")
        Long idArmazenamentoAntigo,
        @NotNull(message = "A quantidade a ser transferida é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade) {}

    public record ItemMovimentacaoDetalhe(
        Long id,
        Long idItem,
        String nomeItem,
        Long idArmazenamentoAntigo,
        String nomeArmazenamentoAntigo,
        Integer quantidade,
        String unidadeMedida) {

        public static ItemMovimentacaoDetalhe deEntidade(MovimentacaoEstoque estoqueMov) {
            return new ItemMovimentacaoDetalhe(
                estoqueMov.getId(),
                estoqueMov.getItem().getId(),
                estoqueMov.getItem().getNome(),
                estoqueMov.getArmazenamentoAntigo().getId(),
                estoqueMov.getArmazenamentoAntigo().getNome(),
                estoqueMov.getQuantidade(),
                estoqueMov.getItem().getUnidadeMedida() != null
                    ? estoqueMov.getItem().getUnidadeMedida().getCodigo()
                    : null);
        }
    }

    public record Requisicao(
        @NotNull(message = "O novo local de armazenamento é obrigatório")
        Long idNovoArmazenamento,
        String descricao,
        @NotEmpty(message = "A movimentação deve possuir ao menos um item")
        @Valid
        List<ItemMovimentacaoRequisicao> itens) {

        public Movimentacao paraEntidade(EnderecoArmazenamento novoArmazenamento) {
            return new Movimentacao(novoArmazenamento, LocalDateTime.now(), this.descricao);
        }
    }

    public record Atualizacao(
        String descricao) {}

    public record Resumo(
        Long id,
        String nomeItem,
        String localAntigo,
        String localNovo,
        String quantidade,
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dataHora) {

        public static Resumo deEntidade(Movimentacao mov, List<MovimentacaoEstoque> itens) {
            if (itens == null || itens.isEmpty()) {
                return new Resumo(
                    mov.getId(),
                    null,
                    null,
                    mov.getNovoArmazenamento() != null ? mov.getNovoArmazenamento().getNome() : null,
                    "0",
                    mov.getDataHora());
            }

            String nomesItens = itens.stream()
                .map(i -> i.getItem() != null ? i.getItem().getNome() : null)
                .filter(nome -> nome != null && !nome.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));

            String locaisAntigos = itens.stream()
                .map(i -> i.getArmazenamentoAntigo() != null ? i.getArmazenamentoAntigo().getNome() : null)
                .filter(nome -> nome != null && !nome.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));

            Map<String, Integer> quantidadesPorUnidade = new TreeMap<>();
            itens.forEach(item -> {
                String unidade = item.getItem() != null && item.getItem().getUnidadeMedida() != null
                    ? item.getItem().getUnidadeMedida().getCodigo().toLowerCase(Locale.ROOT)
                    : "un";
                quantidadesPorUnidade.merge(unidade, item.getQuantidade() != null ? item.getQuantidade() : 0, Integer::sum);
            });
            String quantidadeFormatada = quantidadesPorUnidade.entrySet().stream()
                .map(entry -> entry.getValue() + " " + entry.getKey())
                .collect(java.util.stream.Collectors.joining(", "));

            return new Resumo(
                mov.getId(),
                nomesItens.isEmpty() ? null : nomesItens,
                locaisAntigos.isEmpty() ? null : locaisAntigos,
                mov.getNovoArmazenamento() != null ? mov.getNovoArmazenamento().getNome() : null,
                quantidadeFormatada,
                mov.getDataHora());
        }
    }

    public record Detalhe(
        Long id,
        Long idNovoArmazenamento,
        String nomeNovoArmazenamento,
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dataHora,
        String descricao,
        List<ItemMovimentacaoDetalhe> itens) {

        public static Detalhe deEntidade(Movimentacao mov, List<ItemMovimentacaoDetalhe> itens) {
            return new Detalhe(
                mov.getId(),
                mov.getNovoArmazenamento() != null ? mov.getNovoArmazenamento().getId() : null,
                mov.getNovoArmazenamento() != null ? mov.getNovoArmazenamento().getNome() : null,
                mov.getDataHora(),
                mov.getDescricao(),
                itens != null ? itens : List.of());
        }
    }
    public record MovimentacaoFiltroDTO(
        Long itemId,
        Long localAntigoId,
        Long localNovoId,
        Integer quantidade,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate data,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dataHoraInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime dataHoraFim) {}
}
