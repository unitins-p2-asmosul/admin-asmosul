package br.org.asmosul.api.pessoas.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.pessoas.dtos.CepDTO;
import br.org.asmosul.api.pessoas.models.Uf;
import br.org.asmosul.api.pessoas.services.CepService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CepController.class)
@DisplayName("Testes do CepController (Mockando ViaCEP)")
class CepControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private CepService cepService;

    @Nested
    @DisplayName("GET /cep/{cep} - Consulta de CEP")
    class BuscarCep {

        @Test
        @DisplayName("Deve retornar 200 OK com endereço quando o CEP for válido e encontrado")
        void buscarCep_comCepValidoExistente_retornaEnderecoEStatus200() throws Exception {
            var dto =
                    new CepDTO(
                            "77000-000",
                            "Avenida JK",
                            "Quadra 104 Norte",
                            "Plano Diretor Norte",
                            "Palmas",
                            Uf.TO);

            when(cepService.buscarCep("77000000")).thenReturn(dto);

            mockMvc.perform(get("/cep/{cep}", "77000000").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cep").value("77000-000"))
                    .andExpect(jsonPath("$.logradouro").value("Avenida JK"))
                    .andExpect(jsonPath("$.bairro").value("Plano Diretor Norte"))
                    .andExpect(jsonPath("$.cidade").value("Palmas"))
                    .andExpect(jsonPath("$.uf.codigo").value("TO"));

            verify(cepService).buscarCep("77000000");
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request quando o CEP tiver formato inválido")
        void buscarCep_comCepInvalido_retornaProblemDetailEStatus400() throws Exception {
            when(cepService.buscarCep("123"))
                    .thenThrow(
                            ValidationException.of("cep", "O CEP deve conter 8 dígitos numéricos"));

            mockMvc.perform(get("/cep/{cep}", "123").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.title").value("Erro de Validação"));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found quando o CEP não for encontrado no ViaCEP")
        void buscarCep_comCepInexistenteNoViaCep_retornaStatus404() throws Exception {
            when(cepService.buscarCep("99999999"))
                    .thenThrow(new EntidadeNaoEncontradaException("CEP não encontrado: 99999999"));

            mockMvc.perform(get("/cep/{cep}", "99999999").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}
