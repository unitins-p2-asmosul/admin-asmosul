package br.org.asmosul.api.doacoes.dtos;

import br.org.asmosul.api.doacoes.models.Entrada;
import br.org.asmosul.api.doacoes.models.EntradaEstoque;
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

public final class EntradaDTO {

    private EntradaDTO() {}

    public record ItemEntradaRequisicao(
        @NotNull(message = "O ID do item é obrigatório")
        Long idItem,
        @NotNull(message = "O local de armazenamento é obrigatório")
        Long idArmazenamento,
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade) {}

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
        @NotNull(message = "O doador é obrigatório")
        Long idDoador,
        String descricao,
        @NotEmpty(message = "A entrada deve conter ao menos um item")
        @Valid
        List<ItemEntradaRequisicao> itens) {

        public Entrada paraEntidade(Pessoa doador) {
            return new Entrada(doador, LocalDateTime.now(), this.descricao);
        }
    }

    public record Atualizacao(
        @NotNull(message = "O doador é obrigatório")
        Long idDoador,
        String descricao,
        @NotEmpty(message = "A entrada deve conter ao menos um item")
        @Valid
        List<ItemEntradaRequisicao> itens) {}

    public record Resumo(
        Long id,
        String nomeDoador,
        Integer quantidadeTotalItens,
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dataHora) {

        public static Resumo deEntidade(Entrada entrada, Integer quantidadeTotalItens) {
            return new Resumo(
                entrada.getId(),
                entrada.getDoador() != null ? entrada.getDoador().getNome() : null,
                quantidadeTotalItens != null ? quantidadeTotalItens : 0,
                entrada.getDataHora());
        }
    }

    public record Detalhe(
        Long id,
        Long idDoador,
        String nomeDoador,
        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime dataHora,
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
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate data,
        Long enderecoId) {}
}
