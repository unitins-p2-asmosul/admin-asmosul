package br.org.asmosul.api.pessoas.controllers;

import br.org.asmosul.api.pessoas.dtos.CepDTO;
import br.org.asmosul.api.pessoas.services.CepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CEP", description = "Endpoint para consulta de endereço via CEP (ViaCEP)")
@RestController
@RequestMapping("/cep")
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @Operation(
            summary = "Consultar endereço por CEP",
            description =
                    "Retorna os dados de endereço a partir do CEP informado via integração com"
                            + " ViaCEP")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Endereço retornado com sucesso"),
                @ApiResponse(responseCode = "400", description = "CEP em formato inválido"),
                @ApiResponse(responseCode = "404", description = "CEP não encontrado")
            })
    @GetMapping("/{cep}")
    public ResponseEntity<CepDTO> buscarPorCep(@PathVariable String cep) {
        CepDTO dto = cepService.buscarCep(cep);
        return ResponseEntity.ok(dto);
    }
}
