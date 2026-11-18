package br.org.asmosul.api.relatorios.services;

import br.org.asmosul.api.comum.dtos.ItemGrafico;
import br.org.asmosul.api.comum.dtos.ItemRanking;
import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.relatorios.dtos.RelatorioDoacoesDTO;
import br.org.asmosul.api.relatorios.repositories.RelatorioDoacoesRepository;
import br.org.asmosul.api.relatorios.repositories.projections.AgrupamentoContagemProjection;
import br.org.asmosul.api.relatorios.repositories.projections.BalancoFinanceiroProjection;
import br.org.asmosul.api.relatorios.repositories.projections.CategoriaItemResumoProjection;
import br.org.asmosul.api.relatorios.repositories.projections.EntradaDiscriminadaProjection;
import br.org.asmosul.api.relatorios.repositories.projections.GiroEstoqueProjection;
import br.org.asmosul.api.relatorios.repositories.projections.InventarioEstoqueProjection;
import br.org.asmosul.api.relatorios.repositories.projections.ItemMovimentacaoFinanceiraProjection;
import br.org.asmosul.api.relatorios.repositories.projections.LocaisArmazenamentoProjection;
import br.org.asmosul.api.relatorios.repositories.projections.RankingDoadorProjection;
import br.org.asmosul.api.relatorios.repositories.projections.RankingRecebedorProjection;
import br.org.asmosul.api.relatorios.repositories.projections.SaidaDiscriminadaTabelaProjection;
import br.org.asmosul.api.relatorios.repositories.projections.TotalEstoqueGeralProjection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioDoacoesService {

    private static final BigDecimal CENTAVOS = BigDecimal.valueOf(100);
    private static final Set<String> CAMPOS_FINANCEIRO = Set.of(
            "itemNome", "categoriaNome", "quantidade", "precoUnitarioMedio", "valorTotalFinanceiro");
    private final RelatorioDoacoesRepository repository;

    public RelatorioDoacoesService(RelatorioDoacoesRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public RelatorioDoacoesDTO.ResumoItens obterResumoItens() {
        return new RelatorioDoacoesDTO.ResumoItens(
                mapearRanking(repository.buscarTop3ItensMaisSaem(), 3),
                mapearRanking(repository.buscarTop3ItensMenosSaem(), 3),
                mapearRanking(repository.buscarTop3ItensMaisEntram(), 3),
                mapearRanking(repository.buscarTop3ItensMenosEntram(), 3));
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioDoacoesDTO.GiroEstoqueItem> listarGiroEstoque(
            RelatorioDoacoesDTO.FiltroGiroEstoque filtro, Pageable paginacao) {
        RelatorioDoacoesDTO.FiltroGiroEstoque f = filtro != null
                ? filtro
                : new RelatorioDoacoesDTO.FiltroGiroEstoque(null, null, null, null, null, null, null, null, null, false);
        SortParams ordem = ordenar(paginacao,
                Set.of("nomeItem", "nomeCategoria", "unidadeMedida", "saldoEstoque", "totalEntradas", "totalSaidas"),
                "nomeItem");
        Page<GiroEstoqueProjection> pagina = repository.buscarGiroEstoque(
                texto(f.nomeItem()), f.categoriaId(),
                f.unidadeMedida() != null && !f.unidadeMedida().isBlank()
                        ? UnidadeMedida.deCodigo(f.unidadeMedida()).name() : null,
                f.saldoEstoqueMin(), f.saldoEstoqueMax(),
                f.totalEntradasMin(), f.totalEntradasMax(),
                f.totalSaidasMin(), f.totalSaidasMax(),
                Boolean.TRUE.equals(f.incluirInativos()),
                ordem.campo(), ordem.direcao(), paginaSemSort(paginacao));
        return RespostaPaginada.dePage(pagina.map(p ->
                new RelatorioDoacoesDTO.GiroEstoqueItem(
                        p.getItemId(), p.getItemNome(), p.getCategoriaNome(), codigoMedida(p.getUnidadeMedida()),
                        zero(p.getSaldoEstoque()), zero(p.getTotalEntradas()), zero(p.getTotalSaidas()))));
    }

    @Transactional(readOnly = true)
    public RelatorioDoacoesDTO.ResumoArmazenamento obterResumoArmazenamento() {
        LocaisArmazenamentoProjection totais = repository.buscarTotaisLocaisArmazenamento();
        List<AgrupamentoContagemProjection> grupos = repository.buscarDistribuicaoVolumePorLocal();
        return new RelatorioDoacoesDTO.ResumoArmazenamento(
                totais != null ? zero(totais.getTotalLocais()) : 0L,
                totais != null ? zero(totais.getVolumeFisicoTotal()) : 0L,
                totais != null ? zero(totais.getLocaisEmUso()) : 0L,
                mapearGrafico(grupos));
    }

    @Transactional(readOnly = true)
    public RelatorioDoacoesDTO.ResumoEstoqueGeral obterResumoEstoque() {
        TotalEstoqueGeralProjection totais = repository.buscarTotaisEstoqueGeral();
        return new RelatorioDoacoesDTO.ResumoEstoqueGeral(
                totais != null ? zero(totais.getSaldoFisicoGeral()) : 0L,
                emReais(totais != null ? totais.getValorTotalEstimado() : null));
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioDoacoesDTO.ItemInventarioEstoque> listarInventario(
            RelatorioDoacoesDTO.FiltroInventarioEstoque filtro, Pageable paginacao) {
        RelatorioDoacoesDTO.FiltroInventarioEstoque f = filtro != null
                ? filtro : new RelatorioDoacoesDTO.FiltroInventarioEstoque(null, null, null, null, null);
        SortParams ordem = ordenar(paginacao,
                Set.of("nomeItem", "nomeCategoria", "localArmazenamento", "quantidade", "valorUnitarioEstimado", "valorTotalEstimado"),
                "nomeItem");
        // IN (:ids) não admite uma lista vazia. Um ID inexistente é usado quando o filtro está desligado.
        boolean filtrarCategorias = f.categoriasIds() != null && !f.categoriasIds().isEmpty();
        boolean filtrarLocais = f.locaisIds() != null && !f.locaisIds().isEmpty();
        List<Long> categorias = filtrarCategorias ? f.categoriasIds() : List.of(-1L);
        List<Long> locais = filtrarLocais ? f.locaisIds() : List.of(-1L);
        BigDecimal valorMinimoCentavos = f.valorMinimo() != null
                ? f.valorMinimo().multiply(CENTAVOS) : null;
        Page<InventarioEstoqueProjection> pagina = repository.listarInventarioEstoque(
                texto(f.nomeItem()), categorias, filtrarCategorias,
                locais, filtrarLocais, f.quantidadeMinima(), valorMinimoCentavos,
                ordem.campo(), ordem.direcao(), paginaSemSort(paginacao));
        return RespostaPaginada.dePage(pagina.map(p ->
                new RelatorioDoacoesDTO.ItemInventarioEstoque(
                        p.getIdEstoque(), p.getIdItem(), p.getItemNome(), p.getCategoriaNome(),
                        p.getLocalArmazenamentoNome(), zero(p.getQuantidade()), codigoMedida(p.getUnidadeMedida()),
                        emReais(p.getValorUnitario()), emReais(p.getValorTotal()))));
    }

    @Transactional(readOnly = true)
    public RelatorioDoacoesDTO.ResumoCategoriasItens obterResumoCategorias() {
        List<AgrupamentoContagemProjection> grupos = repository.buscarDistribuicaoCategoriasItens();
        long totalItens = grupos.stream().mapToLong(p -> zero(p.getTotal())).sum();
        return new RelatorioDoacoesDTO.ResumoCategoriasItens(
                totalItens, mapearRanking(grupos, 5), mapearGrafico(grupos));
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioDoacoesDTO.ItemTabelaCategoria> listarCategorias(
            RelatorioDoacoesDTO.FiltroCategoriaItemRelatorio filtro, Pageable paginacao) {
        String termo = filtro != null ? texto(filtro.termoBusca()) : null;
        SortParams ordem = ordenar(paginacao,
                Set.of("nomeCategoria", "itensDistintos", "saldoFisico"), "nomeCategoria");
        Page<CategoriaItemResumoProjection> pagina = repository.listarTabelaCategorias(
                termo, ordem.campo(), ordem.direcao(), paginaSemSort(paginacao));
        return RespostaPaginada.dePage(pagina.map(p ->
                new RelatorioDoacoesDTO.ItemTabelaCategoria(
                        p.getIdCategoria(), p.getNome(), p.getDescricao(),
                        zero(p.getItensDistintos()), zero(p.getSaldoFisico()))));
    }

    @Transactional(readOnly = true)
    public RelatorioDoacoesDTO.ResumoBalancoFinanceiro obterBalancoFinanceiro(
        RelatorioDoacoesDTO.FiltroFinanceiro filtro) {
        RelatorioDoacoesDTO.FiltroFinanceiro f = filtroFinanceiro(filtro);
        BalancoFinanceiroProjection totais = repository.buscarBalancoFinanceiro(
            inicioDoDia(f.dataInicio()),
            fimDoDia(f.dataFim()),
            texto(f.termoItem()),
            texto(f.termoCategoria()));
        BigDecimal saidas = emReais(totais != null ? totais.getTotalSaidas() : null);
        BigDecimal entradas = emReais(totais != null ? totais.getTotalEntradas() : null);
        BigDecimal balanco = entradas.subtract(saidas);
        return new RelatorioDoacoesDTO.ResumoBalancoFinanceiro(
            saidas, entradas, balanco, balanco.signum() >= 0 ? "Superávit" : "Déficit");
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioDoacoesDTO.ItemFinanceiroTabela> listarFinanceiroSaidas(
            RelatorioDoacoesDTO.FiltroFinanceiro filtro, Pageable paginacao) {
        RelatorioDoacoesDTO.FiltroFinanceiro f = filtroFinanceiro(filtro);
        SortParams ordem = ordenar(paginacao, CAMPOS_FINANCEIRO, "valorTotalFinanceiro", "DESC");
        Page<ItemMovimentacaoFinanceiraProjection> pagina = repository.listarFinanceiroSaidas(
                inicioDoDia(f.dataInicio()), fimDoDia(f.dataFim()),
                texto(f.termoItem()), texto(f.termoCategoria()),
                ordem.campo(), ordem.direcao(), paginaSemSort(paginacao));
        return RespostaPaginada.dePage(pagina.map(this::mapearItemFinanceiro));
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioDoacoesDTO.ItemFinanceiroTabela> listarFinanceiroEntradas(
            RelatorioDoacoesDTO.FiltroFinanceiro filtro, Pageable paginacao) {
        RelatorioDoacoesDTO.FiltroFinanceiro f = filtroFinanceiro(filtro);
        SortParams ordem = ordenar(paginacao, CAMPOS_FINANCEIRO, "valorTotalFinanceiro", "DESC");
        Page<ItemMovimentacaoFinanceiraProjection> pagina = repository.listarFinanceiroEntradas(
                inicioDoDia(f.dataInicio()), fimDoDia(f.dataFim()),
                texto(f.termoItem()), texto(f.termoCategoria()),
                ordem.campo(), ordem.direcao(), paginaSemSort(paginacao));
        return RespostaPaginada.dePage(pagina.map(this::mapearItemFinanceiro));
    }

    @Transactional(readOnly = true)
    public RelatorioDoacoesDTO.ResumoEntradas obterResumoEntradas(RelatorioDoacoesDTO.FiltroEntradas filtro) {
        RelatorioDoacoesDTO.FiltroEntradas f = filtroEntradas(filtro);
        LocalDateTime inicio = inicioDoDia(f.dataInicio());
        LocalDateTime fim = fimDoDia(f.dataFim());
        String termo = texto(f.termoBusca());

        TotalEstoqueGeralProjection totais = repository.buscarTotaisEntradas(
            inicio, fim, f.doadorId(), f.localArmazenamentoId(), f.categoriaItemId(), termo);

        List<RelatorioDoacoesDTO.ItemRankingDoador> topDoadores = new ArrayList<>();
        List<RankingDoadorProjection> ranking = repository.buscarTopQuemMaisDoa(
            inicio, fim, f.doadorId(), f.localArmazenamentoId(), f.categoriaItemId(), termo);
        for (int i = 0; i < ranking.size(); i++) {
            RankingDoadorProjection p = ranking.get(i);
            topDoadores.add(new RelatorioDoacoesDTO.ItemRankingDoador(
                i + 1, p.getIdDoador(), p.getNomeDoador(),
                zero(p.getTotalDoacoesRealizadas()), zero(p.getTotalItensDoados())));
        }

        return new RelatorioDoacoesDTO.ResumoEntradas(
            totais != null ? zero(totais.getSaldoFisicoGeral()) : 0L,
            emReais(totais != null ? totais.getValorTotalEstimado() : null),
            mapearMeses(repository.buscarEntradasPorMes(
                anoOuAtual(f.ano()), inicio, fim, f.doadorId(), f.localArmazenamentoId(), f.categoriaItemId(), termo)),
            mapearGrafico(repository.buscarEntradasPorAno(
                inicio, fim, f.doadorId(), f.localArmazenamentoId(), f.categoriaItemId(), termo)),
            topDoadores);
    }

    @Transactional(readOnly = true)
    public RelatorioDoacoesDTO.ResumoSaidas obterResumoSaidas(RelatorioDoacoesDTO.FiltroSaidas filtro) {
        RelatorioDoacoesDTO.FiltroSaidas f = filtroSaidas(filtro);
        LocalDateTime inicio = inicioDoDia(f.dataInicio());
        LocalDateTime fim = fimDoDia(f.dataFim());
        String termo = texto(f.termoBusca());

        TotalEstoqueGeralProjection totais = repository.buscarTotaisSaidas(
            inicio, fim, f.recebedorId(), f.localArmazenamentoId(), f.categoriaItemId(), termo);

        return new RelatorioDoacoesDTO.ResumoSaidas(
            totais != null ? zero(totais.getSaldoFisicoGeral()) : 0L,
            emReais(totais != null ? totais.getValorTotalEstimado() : null),
            mapearMeses(repository.buscarSaidasPorMes(
                anoOuAtual(f.ano()), inicio, fim, f.recebedorId(), f.localArmazenamentoId(), f.categoriaItemId(), termo)),
            mapearGrafico(repository.buscarSaidasPorAno(
                inicio, fim, f.recebedorId(), f.localArmazenamentoId(), f.categoriaItemId(), termo)),
            mapearRankingRecebedores(repository.buscarTopQuemMaisRecebe(
                inicio, fim, f.recebedorId(), f.localArmazenamentoId(), f.categoriaItemId(), termo)),
            mapearRankingRecebedores(repository.buscarTopQuemMenosRecebe(
                inicio, fim, f.recebedorId(), f.localArmazenamentoId(), f.categoriaItemId())));
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioDoacoesDTO.ItemEntradaDiscriminada> listarEntradas(
            RelatorioDoacoesDTO.FiltroEntradas filtro, Pageable paginacao) {
        RelatorioDoacoesDTO.FiltroEntradas f = filtroEntradas(filtro);
        SortParams ordem = ordenar(paginacao,
                Set.of("dataHora", "itemNome", "categoriaNome", "quantidade", "precoUnitario",
                        "valorEstimado", "localArmazenamento", "doadorNome"),
                "dataHora", "DESC");
        Page<EntradaDiscriminadaProjection> pagina = repository.listarEntradasDiscriminadas(
                inicioDoDia(f.dataInicio()), fimDoDia(f.dataFim()), f.doadorId(),
                f.localArmazenamentoId(), f.categoriaItemId(), texto(f.termoBusca()),
                ordem.campo(), ordem.direcao(), paginaSemSort(paginacao));
        return RespostaPaginada.dePage(pagina.map(p ->
                new RelatorioDoacoesDTO.ItemEntradaDiscriminada(
                        p.getItemNome(), p.getCategoriaNome(), zero(p.getQuantidade()),
                        emReais(p.getPrecoUnitario()), emReais(p.getValorEstimado()),
                        p.getEnderecoArmazenamentoNome(), p.getDoadorNome(), p.getCpfCnpj(),
                        p.getDataHora(), p.getDescricao())));
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioDoacoesDTO.ItemSaidaDiscriminada> listarSaidas(
            RelatorioDoacoesDTO.FiltroSaidas filtro, Pageable paginacao) {
        RelatorioDoacoesDTO.FiltroSaidas f = filtroSaidas(filtro);
        SortParams ordem = ordenar(paginacao,
                Set.of("dataHora", "itemNome", "categoriaNome", "quantidade", "precoUnitario",
                        "valorEstimado", "localArmazenamento", "recebedorNome"),
                "dataHora", "DESC");
        Page<SaidaDiscriminadaTabelaProjection> pagina = repository.listarSaidasDiscriminadas(
                inicioDoDia(f.dataInicio()), fimDoDia(f.dataFim()), f.recebedorId(),
                f.localArmazenamentoId(), f.categoriaItemId(), texto(f.termoBusca()),
                ordem.campo(), ordem.direcao(), paginaSemSort(paginacao));
        return RespostaPaginada.dePage(pagina.map(p ->
                new RelatorioDoacoesDTO.ItemSaidaDiscriminada(
                        p.getItemNome(), p.getCategoriaNome(), zero(p.getQuantidade()),
                        emReais(p.getPrecoUnitario()), emReais(p.getValorEstimado()),
                        p.getEnderecoArmazenamentoNome(), p.getRecebedorNome(), p.getCpfCnpj(),
                        p.getDataHora(), p.getDescricao())));
    }

    private RelatorioDoacoesDTO.ItemFinanceiroTabela mapearItemFinanceiro(ItemMovimentacaoFinanceiraProjection p) {
        return new RelatorioDoacoesDTO.ItemFinanceiroTabela(
                p.getItemId(), p.getItemNome(), p.getCategoriaNome(), zero(p.getQuantidadeTotal()),
                codigoMedida(p.getUnidadeMedida()), emReais(p.getPrecoUnitarioMedio()),
                emReais(p.getValorTotalFinanceiro()));
    }

    private List<RelatorioDoacoesDTO.ItemRankingRecebedor> mapearRankingRecebedores(
            List<RankingRecebedorProjection> ranking) {
        List<RelatorioDoacoesDTO.ItemRankingRecebedor> resultado = new ArrayList<>();
        for (int i = 0; i < ranking.size(); i++) {
            RankingRecebedorProjection p = ranking.get(i);
            resultado.add(new RelatorioDoacoesDTO.ItemRankingRecebedor(
                    i + 1, p.getIdRecebedor(), p.getNomeRecebedor(), zero(p.getQuantidadeCoabitantes()),
                    zero(p.getTotalEntregasRecebidas()), zero(p.getTotalItensRecebidos())));
        }
        return resultado;
    }

    // O gráfico mensal exibe os 12 meses do ano, inclusive os que não tiveram movimentação.
    private List<ItemGrafico> mapearMeses(List<AgrupamentoContagemProjection> grupos) {
        Map<String, Long> totaisPorMes = new HashMap<>();
        grupos.forEach(p -> totaisPorMes.put(p.getChave(), zero(p.getTotal())));
        long total = totaisPorMes.values().stream().mapToLong(Long::longValue).sum();
        List<ItemGrafico> meses = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            String chave = String.format("%02d", mes);
            long quantidade = totaisPorMes.getOrDefault(chave, 0L);
            double percentual = total == 0 ? 0.0
                    : Math.round((quantidade * 10000.0) / total) / 100.0;
            meses.add(new ItemGrafico(chave, quantidade, percentual));
        }
        return meses;
    }

    private RelatorioDoacoesDTO.FiltroFinanceiro filtroFinanceiro(RelatorioDoacoesDTO.FiltroFinanceiro filtro) {
        return filtro != null ? filtro : new RelatorioDoacoesDTO.FiltroFinanceiro(null, null, null, null);
    }

    private RelatorioDoacoesDTO.FiltroEntradas filtroEntradas(RelatorioDoacoesDTO.FiltroEntradas filtro) {
        return filtro != null ? filtro
                : new RelatorioDoacoesDTO.FiltroEntradas(null, null, null, null, null, null, null);
    }

    private RelatorioDoacoesDTO.FiltroSaidas filtroSaidas(RelatorioDoacoesDTO.FiltroSaidas filtro) {
        return filtro != null ? filtro
                : new RelatorioDoacoesDTO.FiltroSaidas(null, null, null, null, null, null, null);
    }

    private LocalDateTime inicioDoDia(LocalDate data) {
        return data == null ? null : data.atStartOfDay();
    }

    // A data final é inclusiva: considera todas as movimentações até o fim do dia.
    private LocalDateTime fimDoDia(LocalDate data) {
        return data == null ? null : data.atTime(23, 59, 59);
    }

    private int anoOuAtual(Integer ano) {
        return ano != null ? ano : Year.now().getValue();
    }

    private List<ItemRanking> mapearRanking(List<AgrupamentoContagemProjection> grupos, int limite) {
        if (grupos == null || grupos.isEmpty()) {
            return List.of();
        }
        List<ItemRanking> resultado = new ArrayList<>();
        for (int i = 0; i < Math.min(limite, grupos.size()); i++) {
            AgrupamentoContagemProjection p = grupos.get(i);
            resultado.add(new ItemRanking(i + 1, null, p.getChave(), zero(p.getTotal())));
        }
        return resultado;
    }

    private List<ItemGrafico> mapearGrafico(List<AgrupamentoContagemProjection> grupos) {
        if (grupos == null || grupos.isEmpty()) {
            return List.of();
        }
        long total = grupos.stream().mapToLong(p -> zero(p.getTotal())).sum();
        return grupos.stream().map(p -> {
            long quantidade = zero(p.getTotal());
            double percentual = total == 0 ? 0.0
                    : Math.round((quantidade * 10000.0) / total) / 100.0;
            return new ItemGrafico(p.getChave(), quantidade, percentual);
        }).toList();
    }

    // O preço dos itens é persistido em centavos, enquanto os relatórios exibem reais.
    private BigDecimal emReais(BigDecimal centavos) {
        return (centavos == null ? BigDecimal.ZERO : centavos)
                .divide(CENTAVOS, 2, RoundingMode.HALF_UP);
    }

    private int zero(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private long zero(Long valor) {
        return valor == null ? 0L : valor;
    }

    private String codigoMedida(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return UnidadeMedida.valueOf(valor).getCodigo();
        } catch (IllegalArgumentException ex) {
            return valor;
        }
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Pageable paginaSemSort(Pageable paginacao) {
        if (paginacao == null || paginacao.isUnpaged()) {
            return PageRequest.of(0, 10);
        }
        return PageRequest.of(paginacao.getPageNumber(), paginacao.getPageSize());
    }

    private SortParams ordenar(Pageable paginacao, Set<String> validos, String padrao) {
        return ordenar(paginacao, validos, padrao, "ASC");
    }

    private SortParams ordenar(Pageable paginacao, Set<String> validos, String padrao, String direcaoPadrao) {
        if (paginacao != null && paginacao.getSort().isSorted()) {
            for (Sort.Order ordem : paginacao.getSort()) {
                if (validos.contains(ordem.getProperty())) {
                    return new SortParams(ordem.getProperty(), ordem.isDescending() ? "DESC" : "ASC");
                }
            }
        }
        return new SortParams(padrao, direcaoPadrao);
    }

    private record SortParams(String campo, String direcao) {}
}
