package br.org.asmosul.api.pessoas.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.pessoas.dtos.CategoriaDTO;
import br.org.asmosul.api.pessoas.services.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(
            summary = "Cadastrar uma nova categoria",
            description = "Cria um novo registro de categoria no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Categoria já cadastrada com este nome")
            })
    @PostMapping
    public ResponseEntity<CategoriaDTO.Detalhe> cadastrar(
            @RequestBody @Valid CategoriaDTO.Requisicao requisicao,
            UriComponentsBuilder uriBuilder) {
        CategoriaDTO.Detalhe detalhe = categoriaService.cadastrar(requisicao);
        URI uri = uriBuilder.path("/categorias/{id}").buildAndExpand(detalhe.id()).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar categorias",
            description = "Retorna uma listagem paginada de categorias")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<CategoriaDTO.Resumo>> listar(
            @org.springdoc.core.annotations.ParameterObject
                    @PageableDefault(size = 10, sort = "nome")
                    Pageable paginacao,
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        RespostaPaginada<CategoriaDTO.Resumo> resposta =
                categoriaService.listar(paginacao, incluirInativos);
        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Listar todas as categorias ativas",
            description = "Retorna uma lista simples não paginada com todas as categorias ativas")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            })
    @GetMapping("/todas")
    public ResponseEntity<java.util.List<CategoriaDTO.Resumo>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna os detalhes completos de uma categoria ativa")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria não encontrada ou inativa")
            })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        CategoriaDTO.Detalhe detalhe = categoriaService.buscarPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Atualizar dados da categoria",
            description = "Atualiza as informações de uma categoria cadastrada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria não encontrada ou inativa"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Categoria já cadastrada com este nome")
            })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO.Detalhe> atualizar(
            @PathVariable Long id, @RequestBody @Valid CategoriaDTO.Atualizacao requisicao) {
        CategoriaDTO.Detalhe detalhe = categoriaService.atualizar(id, requisicao);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Desativar categoria",
            description = "Realiza a desativação lógica (soft delete) da categoria")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Categoria desativada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria não encontrada ou já inativa")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        categoriaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar categoria",
            description = "Reativa o registro de uma categoria previamente desativada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Categoria reativada com sucesso"),
                @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        categoriaService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
