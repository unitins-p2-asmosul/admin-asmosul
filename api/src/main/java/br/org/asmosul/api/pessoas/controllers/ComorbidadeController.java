package br.org.asmosul.api.pessoas.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.pessoas.dtos.ComorbidadeDTO;
import br.org.asmosul.api.pessoas.services.ComorbidadeService;
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

@Tag(name = "Comorbidades", description = "Endpoints para gerenciamento de comorbidades")
@RestController
@RequestMapping("/comorbidades")
public class ComorbidadeController {

    private final ComorbidadeService comorbidadeService;

    public ComorbidadeController(ComorbidadeService comorbidadeService) {
        this.comorbidadeService = comorbidadeService;
    }

    @Operation(
            summary = "Cadastrar uma nova comorbidade",
            description = "Cria um novo registro de comorbidade no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Comorbidade criada com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Comorbidade já cadastrada com este nome")
            })
    @PostMapping
    public ResponseEntity<ComorbidadeDTO.Detalhe> cadastrar(
            @RequestBody @Valid ComorbidadeDTO.Requisicao requisicao,
            UriComponentsBuilder uriBuilder) {
        ComorbidadeDTO.Detalhe detalhe = comorbidadeService.cadastrar(requisicao);
        URI uri = uriBuilder.path("/comorbidades/{id}").buildAndExpand(detalhe.id()).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar comorbidades",
            description = "Retorna uma listagem paginada de comorbidades")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<ComorbidadeDTO.Resumo>> listar(
            @org.springdoc.core.annotations.ParameterObject
                    @PageableDefault(size = 10, sort = "nome")
                    Pageable paginacao,
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        RespostaPaginada<ComorbidadeDTO.Resumo> resposta =
                comorbidadeService.listar(paginacao, incluirInativos);
        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Listar todas as comorbidades ativas",
            description = "Retorna uma lista simples não paginada com todas as comorbidades ativas")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            })
    @GetMapping("/todas")
    public ResponseEntity<java.util.List<ComorbidadeDTO.Resumo>> listarTodas() {
        return ResponseEntity.ok(comorbidadeService.listarTodas());
    }

    @Operation(
            summary = "Buscar comorbidade por ID",
            description = "Retorna os detalhes completos de uma comorbidade ativa")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Comorbidade encontrada"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Comorbidade não encontrada ou inativa")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ComorbidadeDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        ComorbidadeDTO.Detalhe detalhe = comorbidadeService.buscarPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Atualizar dados da comorbidade",
            description = "Atualiza as informações de uma comorbidade cadastrada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Comorbidade não encontrada ou inativa"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Comorbidade já cadastrada com este nome")
            })
    @PutMapping("/{id}")
    public ResponseEntity<ComorbidadeDTO.Detalhe> atualizar(
            @PathVariable Long id, @RequestBody @Valid ComorbidadeDTO.Atualizacao requisicao) {
        ComorbidadeDTO.Detalhe detalhe = comorbidadeService.atualizar(id, requisicao);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Desativar comorbidade",
            description = "Realiza a desativação lógica (soft delete) da comorbidade")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Comorbidade desativada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Comorbidade não encontrada ou já inativa")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        comorbidadeService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar comorbidade",
            description = "Reativa o registro de uma comorbidade previamente desativada")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Comorbidade reativada com sucesso"),
                @ApiResponse(responseCode = "404", description = "Comorbidade não encontrada")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        comorbidadeService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
