package br.org.asmosul.api.relatorios.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.relatorios.dtos.RelatorioPessoaDTO;
import br.org.asmosul.api.relatorios.enums.CriterioAgrupamentoPessoa;
import br.org.asmosul.api.relatorios.services.RelatorioPessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Relatórios - Pessoas",
        description =
                "Endpoints de relatórios gerenciais e indicadores demográficos do módulo de pessoas,"
                        + " comorbidades e categorias")
@RestController
@RequestMapping("/relatorios/pessoas")
public class RelatorioPessoaController {

    private final RelatorioPessoaService relatorioPessoaService;

    public RelatorioPessoaController(RelatorioPessoaService relatorioPessoaService) {
        this.relatorioPessoaService = relatorioPessoaService;
    }

    @Operation(
            summary = "Obter resumo geral e demográfico de pessoas",
            description =
                    "Retorna indicadores dos cards superiores (beneficiários, doadores, inativos,"
                            + " cadastros totais) e fatias do gráfico demográfico agrupadas por"
                            + " critério (SEXO, ESCOLARIDADE ou FAIXA_RENDA)")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Resumo geral de pessoas retornado com sucesso")
            })
    @GetMapping("/geral")
    public ResponseEntity<RelatorioPessoaDTO.ResumoGeral> obterResumoGeral(
            @Parameter(
                            description =
                                    "Critério de agrupamento demográfico (SEXO, ESCOLARIDADE ou FAIXA_RENDA)")
                    @RequestParam(required = false, defaultValue = "SEXO")
                    CriterioAgrupamentoPessoa criterio) {
        RelatorioPessoaDTO.ResumoGeral resumo = relatorioPessoaService.obterResumoGeral(criterio);
        return ResponseEntity.ok(resumo);
    }

    @Operation(
            summary = "Obter resumo de comorbidades de pessoas",
            description =
                    "Retorna o total de ocorrências ativas, Top 5 condições mais frequentes e"
                            + " fatias do gráfico de pizza de comorbidades")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Resumo de comorbidades retornado com sucesso")
            })
    @GetMapping("/comorbidades/resumo")
    public ResponseEntity<RelatorioPessoaDTO.ResumoComorbidades> obterResumoComorbidades() {
        RelatorioPessoaDTO.ResumoComorbidades resumo =
                relatorioPessoaService.obterResumoComorbidades();
        return ResponseEntity.ok(resumo);
    }

    @Operation(
            summary = "Listar tabela paginada de pessoas com comorbidades",
            description =
                    "Retorna listagem paginada de pessoas ativas que possuem comorbidades"
                            + " diagnosticadas e informações de contato")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description =
                                "Listagem paginada de pessoas com comorbidades retornada com sucesso")
            })
    @GetMapping("/comorbidades/tabela")
    public ResponseEntity<RespostaPaginada<RelatorioPessoaDTO.PessoaComorbidadeItem>>
            listarPessoasComorbidades(
                    @Parameter(description = "Texto livre para filtrar por nome ou CPF/CNPJ")
                            @RequestParam(required = false)
                            String termoBusca,
                    @Parameter(description = "Lista de IDs de comorbidades para filtro")
                            @RequestParam(required = false)
                            List<Long> comorbidadeIds,
                    @ParameterObject @PageableDefault(size = 10, sort = "nome")
                            Pageable paginacao) {
        RespostaPaginada<RelatorioPessoaDTO.PessoaComorbidadeItem> resposta =
                relatorioPessoaService.listarPessoasComorbidades(
                        termoBusca, comorbidadeIds, paginacao);
        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Obter resumo de categorias de pessoas",
            description =
                    "Retorna o total de vínculos, Top 5 categorias de pessoas mais frequentes e"
                            + " fatias do gráfico circular")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Resumo de categorias de pessoas retornado com sucesso")
            })
    @GetMapping("/categorias")
    public ResponseEntity<RelatorioPessoaDTO.ResumoCategoriasPessoa> obterResumoCategorias() {
        RelatorioPessoaDTO.ResumoCategoriasPessoa resumo =
                relatorioPessoaService.obterResumoCategorias();
        return ResponseEntity.ok(resumo);
    }
}
