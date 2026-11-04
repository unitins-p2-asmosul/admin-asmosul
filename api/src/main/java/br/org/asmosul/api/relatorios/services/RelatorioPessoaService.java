package br.org.asmosul.api.relatorios.services;

import br.org.asmosul.api.comum.dtos.ItemGrafico;
import br.org.asmosul.api.comum.dtos.ItemRanking;
import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.relatorios.dtos.RelatorioPessoaDTO;
import br.org.asmosul.api.relatorios.enums.CriterioAgrupamentoPessoa;
import br.org.asmosul.api.relatorios.repositories.RelatorioPessoaRepository;
import br.org.asmosul.api.relatorios.repositories.projections.AgrupamentoContagemProjection;
import br.org.asmosul.api.relatorios.repositories.projections.PessoaComorbidadeProjection;
import br.org.asmosul.api.relatorios.repositories.projections.TotaisPessoasProjection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioPessoaService {

    private static final int LIMITE_TOP_RANKING = 5;

    private final RelatorioPessoaRepository relatorioPessoaRepository;

    public RelatorioPessoaService(RelatorioPessoaRepository relatorioPessoaRepository) {
        this.relatorioPessoaRepository = relatorioPessoaRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioPessoaDTO.ResumoGeral obterResumoGeral(CriterioAgrupamentoPessoa criterio) {
        CriterioAgrupamentoPessoa criterioEfetivo =
                (criterio != null) ? criterio : CriterioAgrupamentoPessoa.SEXO;

        TotaisPessoasProjection totais = relatorioPessoaRepository.buscarTotaisCards();

        long totalBeneficiarios =
                (totais != null && totais.getTotalBeneficiario() != null)
                        ? totais.getTotalBeneficiario()
                        : 0L;
        long totalDoadores =
                (totais != null && totais.getTotalDoador() != null)
                        ? totais.getTotalDoador()
                        : 0L;
        long totalCadastrosGerais =
                (totais != null && totais.getTotalCadastrosGerais() != null)
                        ? totais.getTotalCadastrosGerais()
                        : 0L;
        long totalInativos =
                (totais != null && totais.getTotalInativo() != null)
                        ? totais.getTotalInativo()
                        : 0L;

        List<AgrupamentoContagemProjection> agrupamentos =
                buscarAgrupamentoPorCriterio(criterioEfetivo);
        long totalPessoasGrafico = calcularTotalGeral(agrupamentos);
        List<ItemGrafico> distribuicao = mapearParaItensGrafico(agrupamentos, totalPessoasGrafico);

        return new RelatorioPessoaDTO.ResumoGeral(
                totalBeneficiarios,
                totalDoadores,
                totalCadastrosGerais,
                totalInativos,
                totalPessoasGrafico,
                distribuicao);
    }

    @Transactional(readOnly = true)
    public RelatorioPessoaDTO.ResumoComorbidades obterResumoComorbidades() {
        List<AgrupamentoContagemProjection> agrupamentos =
                relatorioPessoaRepository.buscarDistribuicaoComorbidades();

        long totalOcorrencias = calcularTotalGeral(agrupamentos);
        List<ItemRanking> top5MaisFrequentes = mapearTopRanking(agrupamentos);
        List<ItemGrafico> distribuicao = mapearParaItensGrafico(agrupamentos, totalOcorrencias);

        return new RelatorioPessoaDTO.ResumoComorbidades(
                totalOcorrencias, top5MaisFrequentes, distribuicao);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioPessoaDTO.PessoaComorbidadeItem> listarPessoasComorbidades(
            RelatorioPessoaDTO.FiltroPessoaComorbidade filtro, Pageable paginacao) {
        String termoBusca = (filtro != null) ? filtro.termoBusca() : null;
        List<Long> comorbidadeIds = (filtro != null) ? filtro.comorbidadeIds() : null;
        return listarPessoasComorbidades(termoBusca, comorbidadeIds, paginacao);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RelatorioPessoaDTO.PessoaComorbidadeItem> listarPessoasComorbidades(
            String termoBusca, List<Long> comorbidadeIds, Pageable paginacao) {
        String termoNormalizado =
                (termoBusca != null && !termoBusca.isBlank()) ? termoBusca.trim() : null;
        List<Long> idsNormalizados =
                (comorbidadeIds != null && !comorbidadeIds.isEmpty()) ? comorbidadeIds : null;

        Pageable paginacaoSemOrdenacaoDuplicada = normalizarPaginacaoNativa(paginacao);

        Page<PessoaComorbidadeProjection> pagina =
                relatorioPessoaRepository.listarPessoasComorbidades(
                        termoNormalizado, idsNormalizados, paginacaoSemOrdenacaoDuplicada);

        Page<RelatorioPessoaDTO.PessoaComorbidadeItem> paginaMapeada =
                pagina.map(this::mapearParaPessoaComorbidadeItem);

        return RespostaPaginada.dePage(paginaMapeada);
    }

    @Transactional(readOnly = true)
    public RelatorioPessoaDTO.ResumoCategoriasPessoa obterResumoCategorias() {
        List<AgrupamentoContagemProjection> agrupamentos =
                relatorioPessoaRepository.buscarDistribuicaoCategoriasPessoa();

        long totalVinculos = calcularTotalGeral(agrupamentos);
        List<ItemRanking> top5MaisFrequentes = mapearTopRanking(agrupamentos);
        List<ItemGrafico> distribuicao = mapearParaItensGrafico(agrupamentos, totalVinculos);

        return new RelatorioPessoaDTO.ResumoCategoriasPessoa(
                totalVinculos, top5MaisFrequentes, distribuicao);
    }

    private List<AgrupamentoContagemProjection> buscarAgrupamentoPorCriterio(
            CriterioAgrupamentoPessoa criterio) {
        List<AgrupamentoContagemProjection> resultado =
                switch (criterio) {
                    case SEXO -> relatorioPessoaRepository.agruparPorSexo();
                    case ESCOLARIDADE -> relatorioPessoaRepository.agruparPorEscolaridade();
                    case FAIXA_RENDA -> relatorioPessoaRepository.agruparPorFaixaRenda();
                };
        return (resultado != null) ? resultado : Collections.emptyList();
    }

    private long calcularTotalGeral(List<AgrupamentoContagemProjection> agrupamentos) {
        if (agrupamentos == null || agrupamentos.isEmpty()) {
            return 0L;
        }
        return agrupamentos.stream()
                .mapToLong(item -> (item != null && item.getTotal() != null) ? item.getTotal() : 0L)
                .sum();
    }

    private List<ItemGrafico> mapearParaItensGrafico(
            List<AgrupamentoContagemProjection> agrupamentos, long totalGeral) {
        if (agrupamentos == null || agrupamentos.isEmpty()) {
            return Collections.emptyList();
        }
        return agrupamentos.stream()
                .map(
                        item -> {
                            long quantidade = (item.getTotal() != null) ? item.getTotal() : 0L;
                            double percentual =
                                    (totalGeral > 0)
                                            ? Math.round((quantidade * 10000.0) / totalGeral)
                                                    / 100.0
                                            : 0.0;
                            return new ItemGrafico(item.getChave(), quantidade, percentual);
                        })
                .toList();
    }

    private List<ItemRanking> mapearTopRanking(List<AgrupamentoContagemProjection> agrupamentos) {
        if (agrupamentos == null || agrupamentos.isEmpty()) {
            return Collections.emptyList();
        }
        int limite = Math.min(agrupamentos.size(), LIMITE_TOP_RANKING);
        List<ItemRanking> ranking = new ArrayList<>(limite);
        for (int i = 0; i < limite; i++) {
            AgrupamentoContagemProjection item = agrupamentos.get(i);
            long quantidade = (item.getTotal() != null) ? item.getTotal() : 0L;
            ranking.add(new ItemRanking(i + 1, null, item.getChave(), quantidade));
        }
        return ranking;
    }

    private Pageable normalizarPaginacaoNativa(Pageable paginacao) {
        if (paginacao == null || paginacao.isUnpaged()) {
            return PageRequest.of(0, 10);
        }
        return PageRequest.of(paginacao.getPageNumber(), paginacao.getPageSize());
    }

    private RelatorioPessoaDTO.PessoaComorbidadeItem mapearParaPessoaComorbidadeItem(
            PessoaComorbidadeProjection projection) {
        String concatenadas = projection.getComorbidadesConcatenadas();
        List<String> comorbidades =
                (concatenadas != null && !concatenadas.isBlank())
                        ? Arrays.stream(concatenadas.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .toList()
                        : Collections.emptyList();

        return new RelatorioPessoaDTO.PessoaComorbidadeItem(
                projection.getIdPessoa(),
                projection.getNome(),
                projection.getCpfCnpj(),
                projection.getTelefone(),
                projection.getSexo(),
                comorbidades);
    }
}
