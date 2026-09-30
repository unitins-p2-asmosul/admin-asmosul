package br.org.asmosul.api.doacoes.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.dtos.EnderecoDoacaoDTO;
import br.org.asmosul.api.doacoes.services.EnderecoDoacaoService;
import br.org.asmosul.api.pessoas.models.Uf;
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
        name = "Endereços de Doação",
        description =
                "Endpoints para gerenciamento de endereços de armazenamento de itens no módulo de doações")
@RestController
@RequestMapping("/doacoes/enderecos")
public class EnderecoDoacaoController {

    private final EnderecoDoacaoService enderecoDoacaoService;

    public EnderecoDoacaoController(EnderecoDoacaoService enderecoDoacaoService) {
        this.enderecoDoacaoService = enderecoDoacaoService;
    }

    @Operation(
            summary = "Cadastrar um novo endereço de armazenamento",
            description =
                    "Cria um novo registro de endereço de armazenamento para doações no sistema")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Endereço cadastrado com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Endereço já cadastrado com este nome")
            })
    @PostMapping
    public ResponseEntity<EnderecoDoacaoDTO.Detalhe> cadastrar(
            @RequestBody @Valid EnderecoDoacaoDTO.Requisicao requisicao,
            UriComponentsBuilder uriBuilder) {
        EnderecoDoacaoDTO.Detalhe detalhe = enderecoDoacaoService.cadastrar(requisicao);
        URI uri =
                uriBuilder
                        .path("/doacoes/enderecos/{id}")
                        .buildAndExpand(detalhe.id())
                        .toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar endereços de armazenamento",
            description =
                    "Retorna uma listagem paginada de endereços de armazenamento com suporte a múltiplos filtros")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<EnderecoDoacaoDTO.Resumo>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao,
            @RequestParam(defaultValue = "false") boolean incluirInativos,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cep,
            @RequestParam(required = false) Uf uf,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String logradouro) {

        RespostaPaginada<EnderecoDoacaoDTO.Resumo> resposta =
                enderecoDoacaoService.listar(
                        paginacao, incluirInativos, nome, cep, uf, cidade, bairro, logradouro);

        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Listar todos os endereços de armazenamento",
            description =
                    "Retorna uma lista simples não paginada com todos os endereços de armazenamento para dropdowns")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
            })
    @GetMapping("/todas")
    public ResponseEntity<List<EnderecoDoacaoDTO.Resumo>> listarTodas(
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        return ResponseEntity.ok(enderecoDoacaoService.listarTodas(incluirInativos));
    }

    @Operation(
            summary = "Buscar endereço de armazenamento por ID",
            description = "Retorna os detalhes completos de um endereço de armazenamento ativo")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Endereço não encontrado ou inativo")
            })
    @GetMapping("/{id}")
    public ResponseEntity<EnderecoDoacaoDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        EnderecoDoacaoDTO.Detalhe detalhe = enderecoDoacaoService.buscarPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Atualizar dados do endereço de armazenamento",
            description = "Atualiza as informações de um endereço de armazenamento cadastrado")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Dados atualizados com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Endereço não encontrado ou inativo"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Endereço já cadastrado com este nome")
            })
    @PutMapping("/{id}")
    public ResponseEntity<EnderecoDoacaoDTO.Detalhe> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid EnderecoDoacaoDTO.Atualizacao requisicao) {
        EnderecoDoacaoDTO.Detalhe detalhe = enderecoDoacaoService.atualizar(id, requisicao);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Desativar endereço de armazenamento",
            description =
                    "Realiza a desativação lógica (soft delete) do endereço caso não haja itens vinculados (RN012)")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Endereço desativado com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Existem itens vinculados a este endereço (RN012)"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Endereço não encontrado ou já inativo")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        enderecoDoacaoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar endereço de armazenamento",
            description =
                    "Reativa o registro de um endereço de armazenamento previamente desativado")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Endereço reativado com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Endereço não encontrado")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        enderecoDoacaoService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
