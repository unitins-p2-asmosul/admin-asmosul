package br.org.asmosul.api.doacoes.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.dtos.CategoriaItemDTO;
import br.org.asmosul.api.doacoes.services.CategoriaItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(
        name = "Categorias de Itens",
        description = "Endpoints para gerenciamento de categorias de itens no módulo de doações")
@RestController
@RequestMapping("/doacoes/categorias")
public class CategoriaItemController {

    private final CategoriaItemService categoriaItemService;

    public CategoriaItemController(CategoriaItemService categoriaItemService) {
        this.categoriaItemService = categoriaItemService;
    }

    @Operation(
            summary = "Cadastrar uma nova categoria de item",
            description = "Cria um novo registro de categoria de item no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Categoria de item criada com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Categoria de item já cadastrada com este nome")
            })
    @PostMapping
    public ResponseEntity<CategoriaItemDTO.Detalhe> cadastrar(
            @RequestBody @Valid CategoriaItemDTO.Requisicao requisicao,
            UriComponentsBuilder uriBuilder) {
        CategoriaItemDTO.Detalhe detalhe = categoriaItemService.cadastrar(requisicao);
        URI uri =
                uriBuilder
                        .path("/doacoes/categorias/{id}")
                        .buildAndExpand(detalhe.id())
                        .toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar categorias de itens",
            description =
                    "Retorna uma listagem paginada de categorias de itens com filtros por nome, descrição e status")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<CategoriaItemDTO.Resumo>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao,
            @RequestParam(defaultValue = "false") boolean incluirInativos,
            @RequestParam(defaultValue = "false") boolean apenasInativos,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String descricao) {

        RespostaPaginada<CategoriaItemDTO.Resumo> resposta =
                categoriaItemService.listar(
                        paginacao, incluirInativos, apenasInativos, nome, descricao);

        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Listar todas as categorias de itens",
            description =
                    "Retorna uma lista simples não paginada com todas as categorias de itens para caixas de seleção")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            })
    @GetMapping("/todas")
    public ResponseEntity<List<CategoriaItemDTO.Resumo>> listarTodas(
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        return ResponseEntity.ok(categoriaItemService.listarTodas(incluirInativos));
    }

    @Operation(
            summary = "Buscar categoria de item por ID",
            description = "Retorna os detalhes completos de uma categoria de item")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Categoria de item encontrada"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria de item não encontrada")
            })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaItemDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        CategoriaItemDTO.Detalhe detalhe = categoriaItemService.buscarPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Atualizar dados da categoria de item",
            description = "Atualiza as informações de uma categoria de item cadastrada")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Dados atualizados com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria de item não encontrada ou inativa"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Categoria de item já cadastrada com este nome")
            })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaItemDTO.Detalhe> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid CategoriaItemDTO.Atualizacao requisicao) {
        CategoriaItemDTO.Detalhe detalhe = categoriaItemService.atualizar(id, requisicao);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Desativar categoria de item",
            description = "Realiza a desativação lógica (soft delete) da categoria de item")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Categoria de item desativada com sucesso"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Categoria de item vinculada a itens e não pode ser inativada"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria de item não encontrada ou já inativa")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        categoriaItemService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar categoria de item",
            description =
                    "Reativa o registro de uma categoria de item previamente desativada")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Categoria de item reativada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria de item não encontrada")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        categoriaItemService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
