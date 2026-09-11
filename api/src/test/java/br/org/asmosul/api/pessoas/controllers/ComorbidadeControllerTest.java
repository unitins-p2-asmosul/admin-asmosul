package br.org.asmosul.api.pessoas.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.pessoas.dtos.ComorbidadeDTO;
import br.org.asmosul.api.pessoas.models.Comorbidade;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("Testes de IntegraÃ§Ã£o - ComorbidadeController")
class ComorbidadeControllerTest extends BaseAPITest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ComorbidadeRepository comorbidadeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        comorbidadeRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /comorbidades - Cadastro de Comorbidade")
    class Cadastrar {

        @Test
        @DisplayName(
                "Deve cadastrar comorbidade com sucesso retornando status 201 e Location header")
        void cadastrar_comDadosValidos_retornaStatus201ELocationHeader() throws Exception {
            var requisicao =
                    new ComorbidadeDTO.Requisicao("HipertensÃ£o", "PressÃ£o alta crÃ´nica");

            mockMvc.perform(
                            post("/comorbidades")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            org.hamcrest.Matchers.matchesPattern(
                                                    ".*/comorbidades/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("HipertensÃ£o"))
                    .andExpect(jsonPath("$.descricao").value("PressÃ£o alta crÃ´nica"));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome estiver em branco")
        void cadastrar_comNomeEmBranco_retornaStatus400() throws Exception {
            var requisicao = new ComorbidadeDTO.Requisicao("", "DescriÃ§Ã£o vÃ¡lida");

            mockMvc.perform(
                            post("/comorbidades")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome exceder 50 caracteres")
        void cadastrar_comNomeExcedendoLimite_retornaStatus400() throws Exception {
            var nomeLongo = "A".repeat(51);
            var requisicao = new ComorbidadeDTO.Requisicao(nomeLongo, "DescriÃ§Ã£o");

            mockMvc.perform(
                            post("/comorbidades")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 409 quando o nome jÃ¡ estiver cadastrado")
        void cadastrar_comNomeDuplicado_retornaStatus409() throws Exception {
            comorbidadeRepository.save(new Comorbidade("Diabetes", "Tipo 2"));

            var requisicao = new ComorbidadeDTO.Requisicao("Diabetes", "Outra descriÃ§Ã£o");

            mockMvc.perform(
                            post("/comorbidades")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }
    }

    @Nested
    @DisplayName("GET /comorbidades - Listagem Paginada")
    class ListarPaginado {

        @Test
        @DisplayName("Deve retornar listagem paginada de comorbidades com status 200")
        void listar_comPaginacao_retornaStatus200EListaPaginada() throws Exception {
            comorbidadeRepository.save(new Comorbidade("Comorbidade 1", "Desc"));
            comorbidadeRepository.save(new Comorbidade("Comorbidade 2", "Desc"));

            mockMvc.perform(get("/comorbidades").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados").isArray())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }

        @Test
        @DisplayName("Deve filtrar inativos por padrÃ£o na listagem")
        void listar_comFiltroIncluirInativos_retornaStatus200() throws Exception {
            comorbidadeRepository.save(new Comorbidade("Comorbidade Ativa", "Desc"));
            Comorbidade inativa = new Comorbidade("Comorbidade Inativa", "Desc");
            inativa.setDataInativo(java.time.LocalDateTime.now());
            comorbidadeRepository.save(inativa);

            mockMvc.perform(get("/comorbidades").param("incluirInativos", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Comorbidade Ativa"));

            mockMvc.perform(get("/comorbidades").param("incluirInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }
    }

    @Nested
    @DisplayName("GET /comorbidades/todas - Listagem NÃ£o Paginada")
    class ListarTodas {

        @Test
        @DisplayName("Deve retornar lista completa de comorbidades com status 200")
        void listarTodas_retornaStatus200EListaCompleta() throws Exception {
            comorbidadeRepository.save(new Comorbidade("Comorbidade A", "Desc"));
            comorbidadeRepository.save(new Comorbidade("Comorbidade B", "Desc"));

            mockMvc.perform(get("/comorbidades/todas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /comorbidades/{id} - Detalhamento")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes da comorbidade com status 200 quando existir")
        void buscarPorId_comIdExistente_retornaStatus200() throws Exception {
            Comorbidade comorbidade = comorbidadeRepository.save(new Comorbidade("Asma", "Desc"));

            mockMvc.perform(get("/comorbidades/{id}", comorbidade.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(comorbidade.getId()))
                    .andExpect(jsonPath("$.nome").value("Asma"));
        }

        @Test
        @DisplayName("Deve retornar status 404 quando o ID nÃ£o existir")
        void buscarPorId_comIdInexistenteOuInativo_retornaStatus404() throws Exception {
            mockMvc.perform(get("/comorbidades/{id}", 99999L)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /comorbidades/{id} - AtualizaÃ§Ã£o")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar dados da comorbidade com status 200")
        void atualizar_comDadosValidos_retornaStatus200() throws Exception {
            Comorbidade comorbidade =
                    comorbidadeRepository.save(new Comorbidade("Nome Antigo", "Desc"));

            var requisicao = new ComorbidadeDTO.Atualizacao("Nome Novo", "Desc Atualizada");

            mockMvc.perform(
                            put("/comorbidades/{id}", comorbidade.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(comorbidade.getId()))
                    .andExpect(jsonPath("$.nome").value("Nome Novo"))
                    .andExpect(jsonPath("$.descricao").value("Desc Atualizada"));
        }

        @Test
        @DisplayName("Deve retornar status 409 ao tentar atualizar para nome jÃ¡ existente")
        void atualizar_comNomeDuplicado_retornaStatus409() throws Exception {
            comorbidadeRepository.save(new Comorbidade("Comorbidade 1", "Desc"));
            Comorbidade c2 = comorbidadeRepository.save(new Comorbidade("Comorbidade 2", "Desc"));

            var requisicao = new ComorbidadeDTO.Atualizacao("Comorbidade 1", "Desc");

            mockMvc.perform(
                            put("/comorbidades/{id}", c2.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar comorbidade inexistente")
        void atualizar_comIdInexistente_retornaStatus404() throws Exception {
            var requisicao = new ComorbidadeDTO.Atualizacao("Nome", "Desc");

            mockMvc.perform(
                            put("/comorbidades/{id}", 99999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /comorbidades/{id}/desativar - DesativaÃ§Ã£o")
    class Desativar {

        @Test
        @DisplayName("Deve desativar comorbidade com sucesso retornando status 204")
        void desativar_comIdExistente_retornaStatus204() throws Exception {
            Comorbidade comorbidade = comorbidadeRepository.save(new Comorbidade("Ativa", "Desc"));

            mockMvc.perform(patch("/comorbidades/{id}/desativar", comorbidade.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar desativar comorbidade inexistente")
        void desativar_comIdInexistenteOuJaInativo_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/comorbidades/{id}/desativar", 99999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /comorbidades/{id}/reativar - ReativaÃ§Ã£o")
    class Reativar {

        @Test
        @DisplayName("Deve reativar comorbidade com sucesso retornando status 204")
        void reativar_comIdExistente_retornaStatus204() throws Exception {
            Comorbidade comorbidade = new Comorbidade("Inativa", "Desc");
            comorbidade.setDataInativo(java.time.LocalDateTime.now());
            comorbidade = comorbidadeRepository.save(comorbidade);

            mockMvc.perform(patch("/comorbidades/{id}/reativar", comorbidade.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar reativar comorbidade inexistente")
        void reativar_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/comorbidades/{id}/reativar", 99999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /comorbidades/{id} - ExclusÃ£o FÃ­sica")
    class Excluir {

        @Test
        @DisplayName("Deve excluir comorbidade com sucesso retornando status 204")
        void excluir_comIdExistente_retornaStatus204() throws Exception {
            Comorbidade comorbidade =
                    comorbidadeRepository.save(new Comorbidade("Para Excluir", "Desc"));

            mockMvc.perform(delete("/comorbidades/{id}", comorbidade.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar excluir comorbidade inexistente")
        void excluir_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(delete("/comorbidades/{id}", 99999L)).andExpect(status().isNotFound());
        }
    }
}
