package br.org.asmosul.api.pessoas.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.pessoas.dtos.PessoaDTO;
import br.org.asmosul.api.pessoas.services.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Tag(name = "Pessoas", description = "Endpoints para gerenciamento de pessoas")
@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @Operation(summary = "Cadastrar uma nova pessoa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pessoa cadastrada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Categoria ou comorbidade não encontrada"),
        @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    })
    @PostMapping
    public ResponseEntity<PessoaDTO.Detalhe> cadastrar(
        @RequestBody @Valid PessoaDTO.Requisicao requisicao,
        UriComponentsBuilder uriBuilder
    ) {
        PessoaDTO.Detalhe detalhe = pessoaService.cadastrar(requisicao);
        URI uri = uriBuilder.path("/pessoas/{id}").buildAndExpand(detalhe.id()).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(summary = "Listar pessoas", description = "Retorna uma listagem paginada de pessoas")
    @GetMapping
    public ResponseEntity<RespostaPaginada<PessoaDTO.Resumo>> listar(
        @PageableDefault(size = 10, sort = "nome") Pageable paginacao,
        @RequestParam(defaultValue = "false") boolean incluirInativos
    ) {
        return ResponseEntity.ok(pessoaService.listar(paginacao, incluirInativos));
    }

    @Operation(summary = "Buscar pessoa por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PessoaDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pessoaService.buscarPorId(id));
    }

    @Operation(summary = "Buscar pessoa por CPF")
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<PessoaDTO.Detalhe> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(pessoaService.buscarPorCpf(cpf));
    }

    @Operation(summary = "Buscar pessoas por nome parcial")
    @GetMapping("/buscar")
    public ResponseEntity<RespostaPaginada<PessoaDTO.Resumo>> buscarPorNome(
        @RequestParam String nome,
        @PageableDefault(size = 10, sort = "nome") Pageable paginacao
    ) {
        return ResponseEntity.ok(pessoaService.buscarPorNome(nome, paginacao));
    }

    @Operation(summary = "Atualizar dados de uma pessoa")
    @PutMapping("/{id}")
    public ResponseEntity<PessoaDTO.Detalhe> atualizar(
        @PathVariable Long id,
        @RequestBody @Valid PessoaDTO.Atualizacao requisicao
    ) {
        return ResponseEntity.ok(pessoaService.atualizar(id, requisicao));
    }

    @Operation(summary = "Desativar (excluir logicamente) uma pessoa")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        pessoaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar uma pessoa previamente desativada")
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        pessoaService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}