package br.org.asmosul.api.relatorios.dtos;

import br.org.asmosul.api.comum.dtos.ItemGrafico;
import br.org.asmosul.api.comum.dtos.ItemRanking;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class RelatorioDoacoesDTO {

    private RelatorioDoacoesDTO() {}

    // --- Itens e Giro de Estoque ---

    @Schema(description = "Resumo dos quatro blocos de ranking da página de itens")
    public record ResumoItens(
        @Schema(description = "Top 3 itens com maior saída acumulada")
        List<ItemRanking> itensMaisSaem,

        @Schema(description = "Top 3 itens com menor saída acumulada")
        List<ItemRanking> itensMenosSaem,

        @Schema(description = "Top 3 itens com maior entrada acumulada")
        List<ItemRanking> itensMaisEntram,

        @Schema(description = "Top 3 itens com menor entrada acumulada")
        List<ItemRanking> itensMenosEntram
    ) {}

    public record FiltroGiroEstoque(
        String nomeItem,
        Long categoriaId,
        @Schema(description = "Código/sigla ou nome da unidade de medida (ex.: PCT ou PACOTE)")
        String unidadeMedida,
        Integer saldoEstoqueMin,
        Integer saldoEstoqueMax,
        Integer totalEntradasMin,
        Integer totalEntradasMax,
        Integer totalSaidasMin,
        Integer totalSaidasMax,
        Boolean incluirInativos
    ) {}

    @Schema(description = "Linha individual da listagem consolidada de giro de estoque")
    public record GiroEstoqueItem(
        Long idItem,
        String nomeItem,
        String nomeCategoria,
        String unidadeMedida,
        Integer saldoEstoque,
        Integer totalEntradas,
        Integer totalSaidas
    ) {}

    // --- Locais de Armazenamento ---

    @Schema(description = "Resumo dos cartões e gráfico da página de locais de armazenamento")
    public record ResumoArmazenamento(
        Long totalLocais,

        Long volumeFisicoTotal,

        Long locaisEmUso,

        List<ItemGrafico> distribuicaoVolumePorLocal
    ) {}

    // --- Inventário Consolidado de Estoque ---

    @Schema(description = "Resumo dos cartões de topo da página consolidada de estoque")
    public record ResumoEstoqueGeral(
        @Schema(description = "Soma física geral de unidades em estoque")
        Long saldoFisicoGeral,

        @Schema(description = "Património total monetário estimado em stock")
        BigDecimal valorTotalEstimado
    ) {}

    @Schema(description = "Filtros aplicáveis à tabela de inventário de estoque por local")
    public record FiltroInventarioEstoque(
        String nomeItem,
        List<Long> categoriasIds,
        List<Long> locaisIds,
        Integer quantidadeMinima,
        BigDecimal valorMinimo
    ) {}

    @Schema(description = "Linha individual da tabela de inventário de estoque por instalação")
    public record ItemInventarioEstoque(
        Long idEstoque,
        Long idItem,
        String nomeItem,
        String nomeCategoria,
        String localArmazenamento,
        Integer quantidade,
        String unidadeMedida,
        BigDecimal valorUnitarioEstimado,
        BigDecimal valorTotalEstimado
    ) {}

    // --- Categorias de Itens ---

    @Schema(description = "Resumo do Top 5 e gráfico de proporção de categorias de itens")
    public record ResumoCategoriasItens(
        Long totalItensCatalogo,
        @Schema(description = "Top 5 categorias com maior variedade de itens cadastrados")
        List<ItemRanking> top5MaisVariedade,
        List<ItemGrafico> distribuicaoParticipacao
    ) {}

    @Schema(description = "Filtro para busca textual na listagem de categorias de itens")
    public record FiltroCategoriaItemRelatorio(
        String termoBusca
    ) {}

    @Schema(description = "Linha da tabela de listagem de categorias com variedade e saldo físico em guarda")
    public record ItemTabelaCategoria(
        Long idCategoria,
        String nomeCategoria,
        String descricao,
        Long itensDistintos,
        Long saldoFisico
    ) {}

    // --- Relatório Financeiro ---

    public record FiltroFinanceiro(
        LocalDate dataInicio,
        LocalDate dataFim,
        String termoItem,
        String termoCategoria
    ) {}

    public record ResumoBalancoFinanceiro(
        BigDecimal somatorioSaidas,
        BigDecimal somatorioEntradas,
        @Schema(description = "Diferença entre entradas e saídas (arrecadações - entregas)")
        BigDecimal balancoPeriodo,
        @Schema(description = "Indicador de resultado: Superávit ou Déficit")
        String statusBalanco
    ) {}

    public record ItemFinanceiroTabela(
        Long itemId,
        String itemNome,
        String categoriaNome,
        Integer quantidade,
        String unidadeMedida,
        BigDecimal precoUnitarioMedio,
        BigDecimal valorTotalFinanceiro
    ) {}

    // --- Relatório de Saídas ---

    @Schema(description = "Filtros aplicáveis ao relatório de saídas e entregas")
    public record FiltroSaidas(
        LocalDate dataInicio,
        LocalDate dataFim,
        Long recebedorId,
        Long localArmazenamentoId,
        Long categoriaItemId,
        String termoBusca,
        Integer ano
    ) {}

    @Schema(description = "Linha do ranking de beneficiários que recebem doações")
    public record ItemRankingRecebedor(
        Integer posicao,
        Long idRecebedor,
        String nomeRecebedor,
        Integer membrosFamilia,
        Long totalEntregasRecebidas,
        Long totalItensRecebidos
    ) {}

    @Schema(description = "Resumo dos indicadores, gráficos temporais e rankings da página de saídas")
    public record ResumoSaidas(
        @Schema(description = "Total absoluto de unidades entregues no período")
        Long totalItensEntregues,

        @Schema(description = "Impacto financeiro estimado total das saídas no período")
        BigDecimal valorEstimadoEntregue,

        @Schema(description = "Barras da distribuição mensal de saídas")
        List<ItemGrafico> saidasPorMes,

        @Schema(description = "Barras do comparativo anual de saídas")
        List<ItemGrafico> saidasPorAno,

        @Schema(description = "Top 10 beneficiários com maior volume recebido")
        List<ItemRankingRecebedor> topMaisRecebem,

        @Schema(description = "Famílias com menor número de atendimentos ou sem doações recentes")
        List<ItemRankingRecebedor> topMenosRecebem
    ) {}

    @Schema(description = "Linha individual da listagem discriminada de saídas/entregas")
    public record ItemSaidaDiscriminada(
        String itemNome,
        String categoriaNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal valorEstimado,
        String localArmazenamento,
        String recebedorNome,
        String cpfCnpj,
        LocalDateTime dataHora,
        String observacao
    ) {}

    // --- Relatório de Entradas ---

    @Schema(description = "Filtros aplicáveis ao relatório de entradas e doações recebidas")
    public record FiltroEntradas(
        LocalDate dataInicio,
        LocalDate dataFim,
        Long doadorId,
        Long localArmazenamentoId,
        Long categoriaItemId,
        String termoBusca,
        Integer ano
    ) {}

    @Schema(description = "Linha do ranking de maiores doadores")
    public record ItemRankingDoador(
        Integer posicao,
        Long idDoador,
        String nomeDoador,
        Long totalDoacoesRealizadas,
        Long totalItensDoados
    ) {}

    @Schema(description = "Resumo dos indicadores, gráficos temporais e ranking da página de entradas")
    public record ResumoEntradas(
        @Schema(description = "Total absoluto de unidades recebidas no período")
        Long totalItensRecebidos,

        @Schema(description = "Valor estimado total arrecadado no período")
        BigDecimal valorEstimadoRecebido,

        @Schema(description = "Barras da distribuição mensal de entradas")
        List<ItemGrafico> entradasPorMes,

        @Schema(description = "Barras do comparativo anual de entradas")
        List<ItemGrafico> entradasPorAno,

        @Schema(description = "Top 10 doadores com maior volume entregue")
        List<ItemRankingDoador> topDoadores
    ) {}

    public record ItemEntradaDiscriminada(
        String itemNome,
        String categoriaNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal valorEstimado,
        String localArmazenamento,
        String doadorNome,
        String cpfCnpj,
        LocalDateTime dataHora,
        String observacao
    ) {}
}
