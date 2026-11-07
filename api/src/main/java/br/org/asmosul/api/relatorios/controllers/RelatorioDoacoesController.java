package br.org.asmosul.api.relatorios.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.relatorios.dtos.RelatorioDoacoesDTO;
import br.org.asmosul.api.relatorios.services.RelatorioDoacoesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Relatórios - Doações", description = "Indicadores e tabelas de itens, estoque, locais e categorias")
@RestController
@RequestMapping("/relatorios/doacoes")
public class RelatorioDoacoesController {

    private final RelatorioDoacoesService relatorioDoacoesService;

    public RelatorioDoacoesController(RelatorioDoacoesService relatorioDoacoesService) {
        this.relatorioDoacoesService = relatorioDoacoesService;
    }

    @Operation(summary = "Top 3 de itens com maiores e menores entradas e saídas")
    @ApiResponse(responseCode = "200", description = "Rankings obtidos")
    @GetMapping("/itens/resumo")
    public ResponseEntity<RelatorioDoacoesDTO.ResumoItens> obterResumoItens() {
        return ResponseEntity.ok(relatorioDoacoesService.obterResumoItens());
    }

    @Operation(summary = "Listar o giro de estoque por item, com filtros e paginação")
    @ApiResponse(responseCode = "200", description = "Tabela de giro obtida")
    @GetMapping("/itens/tabela")
    public ResponseEntity<RespostaPaginada<RelatorioDoacoesDTO.GiroEstoqueItem>> listarGiroEstoque(
            @ParameterObject RelatorioDoacoesDTO.FiltroGiroEstoque filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "nomeItem") Pageable paginacao) {
        return ResponseEntity.ok(relatorioDoacoesService.listarGiroEstoque(filtro, paginacao));
    }

    @Operation(summary = "Obter cartões e distribuição do volume por local de armazenamento")
    @ApiResponse(responseCode = "200", description = "Resumo de armazenamento obtido")
    @GetMapping("/armazenamento")
    public ResponseEntity<RelatorioDoacoesDTO.ResumoArmazenamento> obterResumoArmazenamento() {
        return ResponseEntity.ok(relatorioDoacoesService.obterResumoArmazenamento());
    }

    @Operation(summary = "Obter saldo físico geral e patrimônio estimado em reais")
    @ApiResponse(responseCode = "200", description = "Totais de estoque obtidos")
    @GetMapping("/estoque/totais")
    public ResponseEntity<RelatorioDoacoesDTO.ResumoEstoqueGeral> obterResumoEstoque() {
        return ResponseEntity.ok(relatorioDoacoesService.obterResumoEstoque());
    }

    @Operation(summary = "Listar inventário de estoque por item e local, com filtros e paginação")
    @ApiResponse(responseCode = "200", description = "Tabela de inventário obtida")
    @GetMapping("/estoque/tabela")
    public ResponseEntity<RespostaPaginada<RelatorioDoacoesDTO.ItemInventarioEstoque>> listarInventario(
            @ParameterObject RelatorioDoacoesDTO.FiltroInventarioEstoque filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "nomeItem") Pageable paginacao) {
        return ResponseEntity.ok(relatorioDoacoesService.listarInventario(filtro, paginacao));
    }

    @Operation(summary = "Obter catálogo total, Top 5 categorias e distribuição dos itens")
    @ApiResponse(responseCode = "200", description = "Resumo de categorias obtido")
    @GetMapping("/categorias/resumo")
    public ResponseEntity<RelatorioDoacoesDTO.ResumoCategoriasItens> obterResumoCategorias() {
        return ResponseEntity.ok(relatorioDoacoesService.obterResumoCategorias());
    }

    @Operation(summary = "Listar categorias com variedade de itens e saldo físico, com paginação")
    @ApiResponse(responseCode = "200", description = "Tabela de categorias obtida")
    @GetMapping("/categorias/tabela")
    public ResponseEntity<RespostaPaginada<RelatorioDoacoesDTO.ItemTabelaCategoria>> listarCategorias(
            @ParameterObject RelatorioDoacoesDTO.FiltroCategoriaItemRelatorio filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "nomeCategoria") Pageable paginacao) {
        return ResponseEntity.ok(relatorioDoacoesService.listarCategorias(filtro, paginacao));
    }
}
