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
import br.org.asmosul.api.pessoas.dtos.CategoriaDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("Testes de IntegraÃ§Ã£o - CategoriaController")
class CategoriaControllerTest extends BaseAPITest {

    @Autowired private MockMvc mockMvc;

    @Autowired private CategoriaRepository categoriaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        categoriaRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /categorias - Cadastro de Categoria")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar categoria com sucesso retornando status 201 e Location header")
        void cadastrar_comDadosValidos_retornaStatus201ELocationHeader() throws Exception {
            var requisicao =
                    new CategoriaDTO.Requisicao("SÃ³cio Fundador", "Categoria de fundadores");

            mockMvc.perform(
                            post("/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            org.hamcrest.Matchers.matchesPattern(
                                                    ".*/categorias/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("SÃ³cio Fundador"))
                    .andExpect(jsonPath("$.descricao").value("Categoria de fundadores"));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome estiver em branco")
        void cadastrar_comNomeEmBranco_retornaStatus400() throws Exception {
            var requisicao = new CategoriaDTO.Requisicao("", "DescriÃ§Ã£o vÃ¡lida");

            mockMvc.perform(
                            post("/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome exceder 50 caracteres")
        void cadastrar_comNomeExcedendoLimite_retornaStatus400() throws Exception {
            var nomeLongo = "A".repeat(51);
            var requisicao = new CategoriaDTO.Requisicao(nomeLongo, "DescriÃ§Ã£o");

            mockMvc.perform(
                            post("/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 409 quando o nome jÃ¡ estiver cadastrado")
        void cadastrar_comNomeDuplicado_retornaStatus409() throws Exception {
            categoriaRepository.save(new Categoria("SÃ³cio Efetivo", "DescriÃ§Ã£o inicial"));

            var requisicao = new CategoriaDTO.Requisicao("SÃ³cio Efetivo", "Nova descriÃ§Ã£o");

            mockMvc.perform(
                            post("/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }
    }

    @Nested
    @DisplayName("GET /categorias - Listagem Paginada")
    class ListarPaginado {

        @Test
        @DisplayName("Deve retornar listagem paginada de categorias com status 200")
        void listar_comPaginacao_retornaStatus200EListaPaginada() throws Exception {
            categoriaRepository.save(new Categoria("Categoria 1", "Desc"));
            categoriaRepository.save(new Categoria("Categoria 2", "Desc"));

            mockMvc.perform(get("/categorias").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados").isArray())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }

        @Test
        @DisplayName("Deve filtrar inativos por padrÃ£o na listagem")
        void listar_comFiltroIncluirInativos_retornaStatus200() throws Exception {
            categoriaRepository.save(new Categoria("Categoria Ativa", "Desc"));
            Categoria inativa = new Categoria("Categoria Inativa", "Desc");
            inativa.setDataInativo(java.time.LocalDateTime.now());
            categoriaRepository.save(inativa);

            mockMvc.perform(get("/categorias").param("incluirInativos", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Categoria Ativa"));

            mockMvc.perform(get("/categorias").param("incluirInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }
    }

    @Nested
    @DisplayName("GET /categorias/todas - Listagem NÃ£o Paginada")
    class ListarTodas {

        @Test
        @DisplayName("Deve retornar lista completa de categorias com status 200")
        void listarTodas_retornaStatus200EListaCompleta() throws Exception {
            categoriaRepository.save(new Categoria("Categoria A", "Desc"));
            categoriaRepository.save(new Categoria("Categoria B", "Desc"));

            mockMvc.perform(get("/categorias/todas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /categorias/{id} - Detalhamento")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes da categoria com status 200 quando existir")
        void buscarPorId_comIdExistente_retornaStatus200() throws Exception {
            Categoria categoria = categoriaRepository.save(new Categoria("Categoria X", "Desc X"));

            mockMvc.perform(get("/categorias/{id}", categoria.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(categoria.getId()))
                    .andExpect(jsonPath("$.nome").value("Categoria X"));
        }

        @Test
        @DisplayName("Deve retornar status 404 quando o ID nÃ£o existir")
        void buscarPorId_comIdInexistenteOuInativo_retornaStatus404() throws Exception {
            mockMvc.perform(get("/categorias/{id}", 99999L)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /categorias/{id} - AtualizaÃ§Ã£o")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar dados da categoria com status 200")
        void atualizar_comDadosValidos_retornaStatus200() throws Exception {
            Categoria categoria = categoriaRepository.save(new Categoria("Nome Antigo", "Desc"));

            var requisicao = new CategoriaDTO.Atualizacao("Nome Novo", "Desc Atualizada");

            mockMvc.perform(
                            put("/categorias/{id}", categoria.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(categoria.getId()))
                    .andExpect(jsonPath("$.nome").value("Nome Novo"))
                    .andExpect(jsonPath("$.descricao").value("Desc Atualizada"));
        }

        @Test
        @DisplayName("Deve retornar status 409 ao tentar atualizar para nome jÃ¡ existente")
        void atualizar_comNomeDuplicado_retornaStatus409() throws Exception {
            categoriaRepository.save(new Categoria("Categoria 1", "Desc"));
            Categoria cat2 = categoriaRepository.save(new Categoria("Categoria 2", "Desc"));

            var requisicao = new CategoriaDTO.Atualizacao("Categoria 1", "Desc");

            mockMvc.perform(
                            put("/categorias/{id}", cat2.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar categoria inexistente")
        void atualizar_comIdInexistente_retornaStatus404() throws Exception {
            var requisicao = new CategoriaDTO.Atualizacao("Nome", "Desc");

            mockMvc.perform(
                            put("/categorias/{id}", 99999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /categorias/{id}/desativar - DesativaÃ§Ã£o")
    class Desativar {

        @Test
        @DisplayName("Deve desativar categoria com sucesso retornando status 204")
        void desativar_comIdExistente_retornaStatus204() throws Exception {
            Categoria categoria = categoriaRepository.save(new Categoria("Ativa", "Desc"));

            mockMvc.perform(patch("/categorias/{id}/desativar", categoria.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar desativar categoria inexistente")
        void desativar_comIdInexistenteOuJaInativo_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/categorias/{id}/desativar", 99999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /categorias/{id}/reativar - ReativaÃ§Ã£o")
    class Reativar {

        @Test
        @DisplayName("Deve reativar categoria com sucesso retornando status 204")
        void reativar_comIdExistente_retornaStatus204() throws Exception {
            Categoria categoria = new Categoria("Inativa", "Desc");
            categoria.setDataInativo(java.time.LocalDateTime.now());
            categoria = categoriaRepository.save(categoria);

            mockMvc.perform(patch("/categorias/{id}/reativar", categoria.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar reativar categoria inexistente")
        void reativar_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/categorias/{id}/reativar", 99999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /categorias/{id} - ExclusÃ£o FÃ­sica")
    class Excluir {

        @Test
        @DisplayName("Deve excluir categoria com sucesso retornando status 204")
        void excluir_comIdExistente_retornaStatus204() throws Exception {
            Categoria categoria = categoriaRepository.save(new Categoria("Para Excluir", "Desc"));

            mockMvc.perform(delete("/categorias/{id}", categoria.getId()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar excluir categoria inexistente")
        void excluir_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(delete("/categorias/{id}", 99999L)).andExpect(status().isNotFound());
        }
    }
}
