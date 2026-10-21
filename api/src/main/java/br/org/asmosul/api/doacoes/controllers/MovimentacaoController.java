package br.org.asmosul.api.doacoes.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.dtos.MovimentacaoDTO;
import br.org.asmosul.api.doacoes.services.MovimentacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(
        name = "Movimentações de Estoque",
        description =
                "Endpoints para gerenciamento de movimentações de estoque entre locais de armazenamento")
@RestController
@RequestMapping("/doacoes/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @Operation(
            summary = "Cadastrar uma nova movimentação de estoque",
            description =
                    "Registra uma nova movimentação física de itens entre endereços de armazenamento com transferência atômica de saldos")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Movimentação realizada com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Dados de entrada inválidos, saldo insuficiente ou origem idêntica ao destino"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Item ou endereço de armazenamento não encontrado")
            })
    @PostMapping
    public ResponseEntity<MovimentacaoDTO.Detalhe> cadastrar(
            @RequestBody @Valid MovimentacaoDTO.Requisicao requisicao,
            UriComponentsBuilder uriBuilder) {
        MovimentacaoDTO.Detalhe detalhe = movimentacaoService.cadastrar(requisicao);
        URI uri =
                uriBuilder
                        .path("/doacoes/movimentacoes/{id}")
                        .buildAndExpand(detalhe.id())
                        .toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar movimentações de estoque",
            description =
                    "Retorna uma listagem paginada de movimentações de estoque com suporte a filtros dinâmicos")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<MovimentacaoDTO.Resumo>> listar(
            @ParameterObject
                    @PageableDefault(size = 10, sort = "dataHora", direction = Sort.Direction.DESC)
                    Pageable paginacao,
            @ParameterObject MovimentacaoDTO.MovimentacaoFiltroDTO filtro) {
        RespostaPaginada<MovimentacaoDTO.Resumo> resposta =
                movimentacaoService.listar(filtro, paginacao);
        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Buscar movimentação por ID",
            description =
                    "Retorna os detalhes completos de uma movimentação de estoque e seus itens")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Movimentação encontrada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Movimentação não encontrada com o ID informado")
            })
    @GetMapping("/{id}")
    public ResponseEntity<MovimentacaoDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimentacaoService.buscarPorId(id));
    }

    @Operation(
            summary = "Atualizar descrição de uma movimentação",
            description =
                    "Atualiza restritamente a descrição de uma movimentação de estoque existente")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Descrição da movimentação atualizada com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Movimentação não encontrada com o ID informado")
            })
    @PatchMapping("/{id}")
    public ResponseEntity<MovimentacaoDTO.Detalhe> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid MovimentacaoDTO.Atualizacao atualizacao) {
        return ResponseEntity.ok(movimentacaoService.atualizar(id, atualizacao));
    }
}

