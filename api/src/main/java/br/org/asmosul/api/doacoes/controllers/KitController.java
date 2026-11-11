package br.org.asmosul.api.doacoes.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.dtos.KitDTO;
import br.org.asmosul.api.doacoes.services.KitService;
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
        name = "Kits",
        description = "Endpoints para gerenciamento de kits de itens no módulo de doações")
@RestController
@RequestMapping("/doacoes/kits")
public class KitController {

    private final KitService kitService;

    public KitController(KitService kitService) {
        this.kitService = kitService;
    }

    @Operation(
            summary = "Cadastrar um novo kit",
            description = "Cria um novo kit com seus itens e quantidades configuradas")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Kit criado com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Dados inválidos ou itens duplicados no mesmo kit"),
                @ApiResponse(responseCode = "404", description = "Item informado não encontrado"),
                @ApiResponse(responseCode = "409", description = "Kit já cadastrado com este nome")
            })
    @PostMapping
    public ResponseEntity<KitDTO.Detalhe> cadastrar(
            @RequestBody @Valid KitDTO.Requisicao requisicao, UriComponentsBuilder uriBuilder) {
        KitDTO.Detalhe detalhe = kitService.cadastrar(requisicao);
        URI uri = uriBuilder.path("/doacoes/kits/{id}").buildAndExpand(detalhe.id()).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar kits",
            description =
                    "Retorna uma listagem paginada de kits com suporte a filtros por nome, descrição, itens e status")
    @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    @GetMapping
    public ResponseEntity<RespostaPaginada<KitDTO.Resumo>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao,
            @ParameterObject KitDTO.KitFiltroDTO filtro) {
        return ResponseEntity.ok(kitService.listar(paginacao, filtro));
    }

    @Operation(
            summary = "Listar todos os kits",
            description =
                    "Retorna uma lista simples não paginada com todos os kits ativos para seleção")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping({"/todos", "/todas"})
    public ResponseEntity<List<KitDTO.Resumo>> listarTodos(
            @RequestParam(defaultValue = "false") boolean incluirInativos) {
        return ResponseEntity.ok(kitService.listarTodos(incluirInativos));
    }

    @Operation(
            summary = "Buscar kit por ID",
            description = "Retorna os detalhes de um kit com a lista de itens que o compõem")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Kit encontrado"),
                @ApiResponse(responseCode = "404", description = "Kit não encontrado")
            })
    @GetMapping("/{id}")
    public ResponseEntity<KitDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(kitService.buscarPorId(id));
    }

    @Operation(
            summary = "Atualizar kit",
            description = "Atualiza os dados e a composição de itens de um kit ativo")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Kit atualizado com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Dados inválidos ou itens duplicados no mesmo kit"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Kit ou item não encontrado ou inativo"),
                @ApiResponse(responseCode = "409", description = "Kit já cadastrado com este nome")
            })
    @PutMapping("/{id}")
    public ResponseEntity<KitDTO.Detalhe> atualizar(
            @PathVariable Long id, @RequestBody @Valid KitDTO.Atualizacao requisicao) {
        return ResponseEntity.ok(kitService.atualizar(id, requisicao));
    }

    @Operation(
            summary = "Desativar kit",
            description = "Realiza a inativação lógica (soft delete) do kit")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Kit desativado com sucesso"),
                @ApiResponse(responseCode = "404", description = "Kit não encontrado ou inativo")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        kitService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar kit",
            description = "Reativa o registro de um kit previamente desativado")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Kit reativado com sucesso"),
                @ApiResponse(responseCode = "404", description = "Kit não encontrado")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        kitService.reativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Consultar disponibilidade do kit por local de armazenamento",
            description =
                    "Calcula quantos kits completos podem ser montados com o saldo em estoque do local informado")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Disponibilidade calculada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Kit ou local de armazenamento não encontrado ou inativo")
            })
    @GetMapping("/{id}/disponibilidade")
    public ResponseEntity<KitDTO.Disponibilidade> consultarDisponibilidade(
            @PathVariable Long id, @RequestParam Long localArmazenamentoId) {
        return ResponseEntity.ok(kitService.consultarDisponibilidade(id, localArmazenamentoId));
    }
}
