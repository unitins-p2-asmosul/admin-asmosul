package br.org.asmosul.api.pessoas.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.pessoas.dtos.PessoaDTO;
import br.org.asmosul.api.pessoas.services.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@Tag(name = "Pessoas", description = "Endpoints para gerenciamento de pessoas/associados")
@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @Operation(
            summary = "Cadastrar uma nova pessoa",
            description =
                    "Cria um novo registro de pessoa no sistema vinculando comorbidades e categorias caso informadas")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "409",
                        description = "CPF ou e-mail já cadastrado no sistema")
            })
    @PostMapping
    public ResponseEntity<PessoaDTO.Detalhe> cadastrar(
            @RequestBody @Valid PessoaDTO.Requisicao requisicao, UriComponentsBuilder uriBuilder) {
        PessoaDTO.Detalhe detalhe = pessoaService.cadastrar(requisicao);
        URI uri = uriBuilder.path("/pessoas/{id}").buildAndExpand(detalhe.id()).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(summary = "Listar pessoas", description = "Retorna uma listagem paginada de pessoas")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<PessoaDTO.Resumo>> listar(
            @org.springdoc.core.annotations.ParameterObject
                    @PageableDefault(size = 10, sort = "nome")
                    Pageable paginacao,
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        RespostaPaginada<PessoaDTO.Resumo> resposta =
                pessoaService.listar(paginacao, incluirInativos);
        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Listar todas as pessoas",
            description =
                    "Retorna uma lista simples não paginada com todas as pessoas (podendo incluir inativas)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            })
    @GetMapping("/todas")
    public ResponseEntity<java.util.List<PessoaDTO.Resumo>> listarTodas(
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        return ResponseEntity.ok(pessoaService.listarTodas(incluirInativos));
    }

    @Operation(
            summary = "Buscar pessoa por ID",
            description =
                    "Retorna os detalhes completos de uma pessoa ativa, incluindo suas comorbidades e categorias")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
                @ApiResponse(responseCode = "404", description = "Pessoa não encontrada ou inativa")
            })
    @GetMapping("/{id}")
    public ResponseEntity<PessoaDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        PessoaDTO.Detalhe detalhe = pessoaService.buscarPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Atualizar dados da pessoa",
            description = "Atualiza as informações de uma pessoa cadastrada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Pessoa não encontrada ou inativa"),
                @ApiResponse(
                        responseCode = "409",
                        description = "CPF ou e-mail já cadastrado por outra pessoa")
            })
    @PutMapping("/{id}")
    public ResponseEntity<PessoaDTO.Detalhe> atualizar(
            @PathVariable Long id, @RequestBody @Valid PessoaDTO.Atualizacao requisicao) {
        PessoaDTO.Detalhe detalhe = pessoaService.atualizar(id, requisicao);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Desativar pessoa",
            description = "Realiza a desativação lógica (soft delete) da pessoa")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Pessoa desativada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Pessoa não encontrada ou já inativa")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        pessoaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar pessoa",
            description = "Reativa o registro de uma pessoa previamente desativada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Pessoa reativada com sucesso"),
                @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        pessoaService.reativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Excluir pessoa",
            description = "Realiza a exclusão física definitiva da pessoa no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Pessoa excluída com sucesso"),
                @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pessoaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
