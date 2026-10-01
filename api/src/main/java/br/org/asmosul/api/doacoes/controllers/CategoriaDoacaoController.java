package br.org.asmosul.api.doacoes.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.dtos.CategoriaDoacaoDTO;
import br.org.asmosul.api.doacoes.services.CategoriaDoacaoService;
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
        name = "Categorias de Doação",
        description = "Endpoints para gerenciamento de categorias de itens no módulo de doações")
@RestController
@RequestMapping("/doacoes/categorias")
public class CategoriaDoacaoController {

    private final CategoriaDoacaoService categoriaDoacaoService;

    public CategoriaDoacaoController(CategoriaDoacaoService categoriaDoacaoService) {
        this.categoriaDoacaoService = categoriaDoacaoService;
    }

    @Operation(
            summary = "Cadastrar uma nova categoria de doação",
            description = "Cria um novo registro de categoria de doação no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Categoria de doação criada com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Categoria de doação já cadastrada com este nome")
            })
    @PostMapping
    public ResponseEntity<CategoriaDoacaoDTO.Detalhe> cadastrar(
            @RequestBody @Valid CategoriaDoacaoDTO.Requisicao requisicao,
            UriComponentsBuilder uriBuilder) {
        CategoriaDoacaoDTO.Detalhe detalhe = categoriaDoacaoService.cadastrar(requisicao);
        URI uri =
                uriBuilder
                        .path("/doacoes/categorias/{id}")
                        .buildAndExpand(detalhe.id())
                        .toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar categorias de doação",
            description =
                    "Retorna uma listagem paginada de categorias de doação com suporte a filtros por nome e descrição")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<CategoriaDoacaoDTO.Resumo>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao,
            @RequestParam(defaultValue = "false") boolean incluirInativos,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String descricao) {

        RespostaPaginada<CategoriaDoacaoDTO.Resumo> resposta =
                categoriaDoacaoService.listar(paginacao, incluirInativos, nome, descricao);

        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Listar todas as categorias de doação",
            description =
                    "Retorna uma lista simples não paginada com todas as categorias de doação para caixas de seleção")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            })
    @GetMapping("/todas")
    public ResponseEntity<List<CategoriaDoacaoDTO.Resumo>> listarTodas(
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        return ResponseEntity.ok(categoriaDoacaoService.listarTodas(incluirInativos));
    }

    @Operation(
            summary = "Buscar categoria de doação por ID",
            description = "Retorna os detalhes completos de uma categoria de doação ativa")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Categoria de doação encontrada"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria de doação não encontrada ou inativa")
            })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDoacaoDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        CategoriaDoacaoDTO.Detalhe detalhe = categoriaDoacaoService.buscarPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Atualizar dados da categoria de doação",
            description = "Atualiza as informações de uma categoria de doação cadastrada")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Dados atualizados com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria de doação não encontrada ou inativa"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Categoria de doação já cadastrada com este nome")
            })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDoacaoDTO.Detalhe> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid CategoriaDoacaoDTO.Atualizacao requisicao) {
        CategoriaDoacaoDTO.Detalhe detalhe = categoriaDoacaoService.atualizar(id, requisicao);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Desativar categoria de doação",
            description = "Realiza a desativação lógica (soft delete) da categoria de doação")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Categoria de doação desativada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria de doação não encontrada ou já inativa")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        categoriaDoacaoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar categoria de doação",
            description =
                    "Reativa o registro de uma categoria de doação previamente desativada")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Categoria de doação reativada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria de doação não encontrada")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        categoriaDoacaoService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
