package br.org.asmosul.api.doacoes.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.dtos.EntradaDTO;
import br.org.asmosul.api.doacoes.services.EntradaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Entradas", description = "Endpoints para gerenciamento de entradas de doações")
@RestController
@RequestMapping("/doacoes/entradas")
public class EntradaController {

    private final EntradaService entradaService;

    public EntradaController(EntradaService entradaService) {
        this.entradaService = entradaService;
    }

    @Operation(
            summary = "Cadastrar uma entrada",
            description =
                    "Cria uma entrada, registra os itens nos locais informados e incrementa o estoque")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Entrada criada com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Doador, item ou endereço não encontrado")
            })
    @PostMapping
    public ResponseEntity<EntradaDTO.Detalhe> cadastrar(
            @RequestBody @Valid EntradaDTO.Requisicao requisicao,
            UriComponentsBuilder uriBuilder) {
        EntradaDTO.Detalhe detalhe = entradaService.cadastrar(requisicao);
        URI uri =
                uriBuilder
                        .path("/doacoes/entradas/{id}")
                        .buildAndExpand(detalhe.id())
                        .toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar entradas",
            description =
                    "Retorna uma listagem paginada com filtros por item, quantidade, doador, data e endereço")
    @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    @GetMapping
    public ResponseEntity<RespostaPaginada<EntradaDTO.Resumo>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable paginacao,
            @ParameterObject EntradaDTO.EntradaFiltroDTO filtro) {
        return ResponseEntity.ok(entradaService.listar(paginacao, filtro));
    }

    @Operation(summary = "Buscar entrada por ID", description = "Retorna os detalhes da entrada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Entrada encontrada"),
                @ApiResponse(responseCode = "404", description = "Entrada não encontrada")
            })
    @GetMapping("/{id}")
    public ResponseEntity<EntradaDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(entradaService.buscarPorId(id));
    }

    @Operation(
            summary = "Atualizar entrada",
            description = "Atualiza parcialmente somente o doador e a descrição da entrada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Entrada atualizada com sucesso"),
                @ApiResponse(responseCode = "404", description = "Entrada ou doador não encontrado")
            })
    @PatchMapping("/{id}")
    public ResponseEntity<EntradaDTO.Detalhe> atualizar(
            @PathVariable Long id, @RequestBody @Valid EntradaDTO.Atualizacao atualizacao) {
        return ResponseEntity.ok(entradaService.atualizar(id, atualizacao));
    }

    @Operation(
            summary = "Excluir entrada",
            description = "Exclui a entrada e estorna do estoque todos os itens relacionados")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Entrada excluída com sucesso"),
                @ApiResponse(responseCode = "404", description = "Entrada não encontrada")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        entradaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Excluir item individual da entrada",
            description =
                    "Exclui um registro de entrada em estoque e estorna a respectiva quantidade")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Registro excluído com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Entrada ou registro de estoque não encontrado")
            })
    @DeleteMapping("/{idEntrada}/estoque/{idEntradaEstoque}")
    public ResponseEntity<Void> deletarItemEntrada(
            @PathVariable Long idEntrada, @PathVariable Long idEntradaEstoque) {
        entradaService.deletarItemEntrada(idEntrada, idEntradaEstoque);
        return ResponseEntity.noContent().build();
    }
}
