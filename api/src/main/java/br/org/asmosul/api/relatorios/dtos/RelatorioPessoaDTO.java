package br.org.asmosul.api.relatorios.dtos;

import br.org.asmosul.api.comum.dtos.ItemGrafico;
import br.org.asmosul.api.comum.dtos.ItemRanking;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public final class RelatorioPessoaDTO {

    private RelatorioPessoaDTO() {}

    public record ResumoGeral(
        Long totalBeneficiarios,
        Long totalDoadores,
        Long totalCadastrosGerais,
        Long totalInativos,
        Long totalPessoasGrafico,
        List<ItemGrafico> distribuicao
    ) {}

    // --- Comorbidades ---

    public record ResumoComorbidades(
        Long totalOcorrencias,
        List<ItemRanking> top5MaisFrequentes,
        List<ItemGrafico> distribuicao
    ) {}

    public record FiltroPessoaComorbidade(
        String termoBusca,
        List<Long> comorbidadeIds
    ) {}

    public record PessoaComorbidadeItem(
        Long idPessoa,
        String nome,
        String cpf,
        String contato,
        String genero,
        List<String> comorbidadesDiagnosticadas
    ) {}

    // --- Categorias ---

    public record ResumoCategoriasPessoa(
        Long totalVinculos,
        List<ItemRanking> top5MaisFrequentes,
        List<ItemGrafico> distribuicao
    ) {}

}
