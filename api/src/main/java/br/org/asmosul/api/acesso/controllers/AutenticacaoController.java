package br.org.asmosul.api.acesso.controllers;

import br.org.asmosul.api.acesso.dtos.AutenticacaoDTO;
import br.org.asmosul.api.acesso.services.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Endpoints de autenticação e emissão de token JWT")
@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @Operation(
            summary = "Realizar login",
            description =
                    "Autentica uma conta ativa com nome de usuário e senha e retorna um token JWT")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso"),
                @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
                @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou conta inativa")
            })
    @PostMapping("/login")
    public ResponseEntity<AutenticacaoDTO.LoginResposta> login(
            @RequestBody @Valid AutenticacaoDTO.LoginRequisicao requisicao) {
        return ResponseEntity.ok(autenticacaoService.autenticar(requisicao));
    }
}
