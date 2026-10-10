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
import java.util.List;

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
        Integer quantidade) {

        public static ItemMovimentacaoDetalhe deEntidade(MovimentacaoEstoque estoqueMov) {
            return new ItemMovimentacaoDetalhe(
                estoqueMov.getId(),
                estoqueMov.getItem().getId(),
                estoqueMov.getItem().getNome(),
                estoqueMov.getArmazenamentoAntigo().getId(),
                estoqueMov.getArmazenamentoAntigo().getNome(),
                estoqueMov.getQuantidade());
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
        @NotNull(message = "O novo local de armazenamento é obrigatório")
        Long idNovoArmazenamento,
        String descricao,
        @NotEmpty(message = "A movimentação deve possuir ao menos um item")
        @Valid
        List<ItemMovimentacaoRequisicao> itens) {}

    public record Resumo(
        Long id,
        String nomeItem,
        String localAntigo,
        String localNovo,
        Integer quantidade,
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dataHora) {

        public static Resumo deEntidade(
            Movimentacao mov,
            String nomeItem,
            String localAntigo,
            Integer quantidade) {
            return new Resumo(
                mov.getId(),
                nomeItem,
                localAntigo,
                mov.getNovoArmazenamento() != null ? mov.getNovoArmazenamento().getNome() : null,
                quantidade,
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
        LocalDate data) {}
}
