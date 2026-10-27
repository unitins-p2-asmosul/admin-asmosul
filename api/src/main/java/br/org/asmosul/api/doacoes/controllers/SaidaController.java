package br.org.asmosul.api.doacoes.controllers;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.doacoes.dtos.SaidaDTO;
import br.org.asmosul.api.doacoes.services.SaidaService;
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

@Tag(name = "Saídas", description = "Endpoints para gerenciamento de saídas de doações")
@RestController
@RequestMapping("/doacoes/saidas")
public class SaidaController {

    private final SaidaService saidaService;

    public SaidaController(SaidaService saidaService) {
        this.saidaService = saidaService;
    }

    @Operation(
            summary = "Cadastrar uma saída",
            description =
                    "Cria uma saída, valida o saldo disponível de cada item no endereço informado e dá baixa no estoque")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Saída criada com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Dados inválidos ou saldo insuficiente em estoque"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Recebedor, item ou endereço não encontrado")
            })
    @PostMapping
    public ResponseEntity<SaidaDTO.Detalhe> cadastrar(
            @RequestBody @Valid SaidaDTO.Requisicao requisicao, UriComponentsBuilder uriBuilder) {
        SaidaDTO.Detalhe detalhe = saidaService.cadastrar(requisicao);
        URI uri = uriBuilder.path("/doacoes/saidas/{id}").buildAndExpand(detalhe.id()).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar saídas",
            description =
                    "Retorna uma listagem paginada com filtros por itens, quantidade, recebedor e data")
    @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
    @GetMapping
    public ResponseEntity<RespostaPaginada<SaidaDTO.Resumo>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable paginacao,
            @ParameterObject SaidaDTO.SaidaFiltroDTO filtro) {
        return ResponseEntity.ok(saidaService.listar(paginacao, filtro));
    }

    @Operation(summary = "Buscar saída por ID", description = "Retorna os detalhes da saída")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Saída encontrada"),
                @ApiResponse(responseCode = "404", description = "Saída não encontrada")
            })
    @GetMapping("/{id}")
    public ResponseEntity<SaidaDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(saidaService.buscarPorId(id));
    }

    @Operation(
            summary = "Atualizar saída",
            description = "Atualiza parcialmente somente o recebedor e a descrição da saída")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Saída atualizada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Saída ou recebedor não encontrado")
            })
    @PatchMapping("/{id}")
    public ResponseEntity<SaidaDTO.Detalhe> atualizar(
            @PathVariable Long id, @RequestBody @Valid SaidaDTO.Atualizacao atualizacao) {
        return ResponseEntity.ok(saidaService.atualizar(id, atualizacao));
    }

    @Operation(
            summary = "Excluir saída",
            description = "Exclui a saída e estorna ao estoque todos os itens relacionados")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Saída excluída com sucesso"),
                @ApiResponse(responseCode = "404", description = "Saída não encontrada")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        saidaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Excluir item individual da saída",
            description =
                    "Exclui um registro de saída em estoque e estorna a respectiva quantidade. O último item da saída não pode ser removido individualmente")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Registro excluído com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description = "O registro é o último item da saída"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Saída ou registro de estoque não encontrado")
            })
    @DeleteMapping("/{idSaida}/estoque/{idSaidaEstoque}")
    public ResponseEntity<Void> deletarItemSaida(
            @PathVariable Long idSaida, @PathVariable Long idSaidaEstoque) {
        saidaService.deletarItemSaida(idSaida, idSaidaEstoque);
        return ResponseEntity.noContent().build();
    }
}
