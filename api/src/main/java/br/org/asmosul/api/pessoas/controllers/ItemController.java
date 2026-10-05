package br.org.asmosul.api.pessoas.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.pessoas.dtos.ItemDTO;
import br.org.asmosul.api.pessoas.dtos.ItemFiltroDTO;
import br.org.asmosul.api.pessoas.services.ItemService;
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

@Tag(name = "Itens", description = "Endpoints para gerenciamento de itens e estoque")
@RestController
@RequestMapping("/itens")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(
            summary = "Cadastrar um novo item",
            description = "Cria um novo registro de item no sistema vinculado a uma categoria")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Item cadastrado com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Categoria informada não encontrada"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Já existe um item cadastrado com esse nome")
            })
    @PostMapping
    public ResponseEntity<ItemDTO.Detalhe> cadastrar(
            @RequestBody @Valid ItemDTO.Requisicao requisicao, UriComponentsBuilder uriBuilder) {
        ItemDTO.Detalhe detalhe = itemService.cadastrar(requisicao);
        URI uri = uriBuilder.path("/itens/{id}").buildAndExpand(detalhe.id()).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar itens",
            description = "Retorna uma listagem paginada e filtrada de itens e seus saldos de estoque")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<ItemDTO.Resumo>> listar(
            @ParameterObject ItemFiltroDTO filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao,
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        RespostaPaginada<ItemDTO.Resumo> resposta =
                itemService.listar(filtro, paginacao, incluirInativos);
        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Buscar item por ID",
            description = "Retorna os detalhes completos de um item ativo e seu saldo de estoque")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Item encontrado"),
                @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        ItemDTO.Detalhe detalhe = itemService.buscarPorId(id);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Atualizar dados do item",
            description = "Atualiza as informações de um item cadastrado")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Dados atualizados com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Item ou Categoria não encontrada"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Já existe outro item cadastrado com esse mesmo nome")
            })
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO.Detalhe> atualizar(
            @PathVariable Long id, @RequestBody @Valid ItemDTO.Atualizacao requisicao) {
        ItemDTO.Detalhe detalhe = itemService.atualizar(id, requisicao);
        return ResponseEntity.ok(detalhe);
    }

    @Operation(
            summary = "Desativar item",
            description = "Realiza a desativação lógica (soft delete) do item")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Item desativado com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Item não encontrado ou já inativo")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        itemService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar item",
            description = "Reativa o registro de um item previamente desativado")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Item reativado com sucesso"),
                @ApiResponse(responseCode = "404", description = "Item não encontrado")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        itemService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}