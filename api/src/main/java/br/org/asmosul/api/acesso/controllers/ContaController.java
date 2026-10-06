package br.org.asmosul.api.acesso.controllers;

import br.org.asmosul.api.acesso.dtos.ContaDTO;
import br.org.asmosul.api.acesso.dtos.ContaFiltroDTO;
import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.acesso.services.ContaService;
import br.org.asmosul.api.comum.dtos.RespostaPaginada;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Contas", description = "Endpoints para gerenciamento de contas de acesso e perfis")
@RestController
@RequestMapping("/acessos/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @Operation(
            summary = "Cadastrar uma nova conta",
            description =
                    "Cria uma conta para uma pessoa física ativa com a senha temporária definida"
                            + " pelo Gerenciador de Acesso. A conta nasce com redefinirSenha = true")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description =
                                "Dados inválidos, pessoa inativa, pessoa jurídica ou senha acima"
                                        + " de 72 bytes"),
                @ApiResponse(responseCode = "404", description = "Pessoa não encontrada"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Nome de usuário já utilizado ou pessoa já possui conta")
            })
    @PostMapping
    public ResponseEntity<ContaDTO.Detalhe> cadastrar(
            @RequestBody @Valid ContaDTO.Requisicao requisicao, UriComponentsBuilder uriBuilder) {
        ContaDTO.Detalhe detalhe = contaService.cadastrar(requisicao);
        URI uri = uriBuilder.path("/contas/{id}").buildAndExpand(detalhe.id()).toUri();
        return ResponseEntity.created(uri).body(detalhe);
    }

    @Operation(
            summary = "Listar contas",
            description =
                    "Retorna uma listagem paginada e filtrada de contas. Com apenasInativos=true,"
                            + " lista somente as contas desativadas e habilita o filtro dataInativo")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Listagem retornada com sucesso")
            })
    @GetMapping
    public ResponseEntity<RespostaPaginada<ContaDTO.Resumo>> listar(
            @ParameterObject ContaFiltroDTO filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "nomeUsuario") Pageable paginacao) {
        return ResponseEntity.ok(contaService.listar(filtro, paginacao));
    }

    @Operation(
            summary = "Listar perfis de acesso",
            description = "Retorna as opções estáticas de perfis com código e descrição")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Perfis retornados com sucesso")})
    @GetMapping("/perfis")
    public ResponseEntity<List<Perfil>> listarPerfis() {
        return ResponseEntity.ok(contaService.listarPerfis());
    }

    @Operation(
            summary = "Buscar conta por ID",
            description =
                    "Retorna os detalhes da conta, ativa ou inativa, sem expor a senha ou o hash")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Conta encontrada"),
                @ApiResponse(responseCode = "404", description = "Conta não encontrada")
            })
    @GetMapping("/{id}")
    public ResponseEntity<ContaDTO.Detalhe> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @Operation(
            summary = "Atualizar nome de usuário",
            description =
                    "Altera apenas o nome de usuário da conta; a pessoa vinculada não pode ser"
                            + " trocada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                @ApiResponse(responseCode = "404", description = "Conta não encontrada ou inativa"),
                @ApiResponse(
                        responseCode = "409",
                        description = "Nome de usuário já utilizado por outra conta")
            })
    @PutMapping("/{id}")
    public ResponseEntity<ContaDTO.Detalhe> atualizar(
            @PathVariable Long id, @RequestBody @Valid ContaDTO.Atualizacao requisicao) {
        return ResponseEntity.ok(contaService.atualizar(id, requisicao));
    }

    @Operation(
            summary = "Atualizar perfis da conta",
            description = "Substitui o conjunto de perfis de acesso da conta")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Perfis atualizados com sucesso"),
                @ApiResponse(responseCode = "400", description = "Lista de perfis vazia ou inválida"),
                @ApiResponse(responseCode = "404", description = "Conta não encontrada ou inativa")
            })
    @PutMapping("/{id}/perfis")
    public ResponseEntity<ContaDTO.Detalhe> atualizarPerfis(
            @PathVariable Long id, @RequestBody @Valid ContaDTO.AtualizacaoPerfis requisicao) {
        return ResponseEntity.ok(contaService.atualizarPerfis(id, requisicao));
    }

    @Operation(
            summary = "Redefinir senha da conta",
            description =
                    "Define uma nova senha temporária para a conta e marca redefinirSenha = true")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Senha em branco ou acima de 72 bytes"),
                @ApiResponse(responseCode = "404", description = "Conta não encontrada ou inativa")
            })
    @PatchMapping("/{id}/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(
            @PathVariable Long id, @RequestBody @Valid ContaDTO.RedefinirSenhaAdmin requisicao) {
        contaService.redefinirSenhaAdmin(id, requisicao);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Obter dados da própria conta",
        description = "Retorna os detalhes da conta do usuário autenticado no token")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Dados da conta retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou inativa")
        })

    @GetMapping("/eu")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContaDTO.Detalhe> obterMinhaConta(Authentication autenticacao) {
        return ResponseEntity.ok(contaService.buscarPorNomeUsuario(autenticacao.getName()));
    }

    @Operation(
            summary = "Alterar a própria senha",
            description =
                    "O usuário autenticado informa a senha atual e define a nova senha. A conta"
                            + " é identificada pelo token e passa a ter redefinirSenha = false")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
                @ApiResponse(
                        responseCode = "400",
                        description =
                                "Senha atual incorreta, campos em branco ou nova senha acima de 72"
                                        + " bytes"),
                @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Conta ativa não encontrada para o usuário autenticado")
            })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/minha-senha")
    public ResponseEntity<Void> alterarMinhaSenha(
            @RequestBody @Valid ContaDTO.RedefinirSenhaPropria requisicao,
            Authentication autenticacao) {
        contaService.alterarSenhaPropria(autenticacao.getName(), requisicao);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Desativar conta",
            description = "Realiza a desativação lógica (soft delete) da conta")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Conta desativada com sucesso"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Conta não encontrada ou já inativa")
            })
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        contaService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reativar conta",
            description =
                    "Reativa uma conta desativada, desde que a pessoa vinculada esteja ativa."
                            + " Reativar uma conta já ativa não altera nada")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Conta reativada com sucesso"),
                @ApiResponse(responseCode = "400", description = "A pessoa vinculada está inativa"),
                @ApiResponse(responseCode = "404", description = "Conta não encontrada")
            })
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        contaService.reativar(id);
        return ResponseEntity.noContent().build();
    }
}
