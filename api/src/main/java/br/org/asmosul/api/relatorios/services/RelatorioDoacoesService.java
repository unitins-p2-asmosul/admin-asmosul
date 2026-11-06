package br.org.asmosul.api.relatorios.services;

import br.org.asmosul.api.comum.dtos.ItemGrafico;
import br.org.asmosul.api.comum.dtos.ItemRanking;
import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.relatorios.dtos.RelatorioDoacoesDTO;
import br.org.asmosul.api.relatorios.repositories.RelatorioDoacoesRepository;
import br.org.asmosul.api.relatorios.repositories.projections.AgrupamentoContagemProjection;
import br.org.asmosul.api.relatorios.repositories.projections.CategoriaItemResumoProjection;
import br.org.asmosul.api.relatorios.repositories.projections.GiroEstoqueProjection;
import br.org.asmosul.api.relatorios.repositories.projections.InventarioEstoqueProjection;
import br.org.asmosul.api.relatorios.repositories.projections.LocaisArmazenamentoProjection;
import br.org.asmosul.api.relatorios.repositories.projections.TotalEstoqueGeralProjection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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
        if (paginacao != null && paginacao.getSort().isSorted()) {
            for (Sort.Order ordem : paginacao.getSort()) {
                if (validos.contains(ordem.getProperty())) {
                    return new SortParams(ordem.getProperty(), ordem.isDescending() ? "DESC" : "ASC");
                }
            }
        }
        return new SortParams(padrao, "ASC");
    }

    private record SortParams(String campo, String direcao) {}
}
