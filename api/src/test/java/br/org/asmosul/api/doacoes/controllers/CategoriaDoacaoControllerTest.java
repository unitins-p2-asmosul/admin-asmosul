package br.org.asmosul.api.doacoes.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.doacoes.dtos.CategoriaDoacaoDTO;
import br.org.asmosul.api.doacoes.models.CategoriaDoacao;
import br.org.asmosul.api.doacoes.repositories.CategoriaDoacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("Testes de Integração - CategoriaDoacaoController")
class CategoriaDoacaoControllerTest extends BaseAPITest {

    @Autowired private CategoriaDoacaoRepository categoriaDoacaoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        categoriaDoacaoRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /doacoes/categorias - Cadastro de Categoria de Doação")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar categoria de doação com sucesso retornando status 201 e Location header")
        void cadastrar_comDadosValidos_retornaStatus201ELocationHeader() throws Exception {
            var requisicao =
                    new CategoriaDoacaoDTO.Requisicao("Alimentos Não Perecíveis", "Arroz, feijão, etc.");

            mockMvc.perform(
                            post("/doacoes/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            org.hamcrest.Matchers.matchesPattern(
                                                    ".*/doacoes/categorias/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("Alimentos Não Perecíveis"))
                    .andExpect(jsonPath("$.descricao").value("Arroz, feijão, etc."))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andExpect(jsonPath("$.dataInativo").doesNotExist());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome estiver em branco")
        void cadastrar_comNomeEmBranco_retornaStatus400() throws Exception {
            var requisicao = new CategoriaDoacaoDTO.Requisicao("", "Descrição válida");

            mockMvc.perform(
                            post("/doacoes/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome exceder 50 caracteres")
        void cadastrar_comNomeExcedendoLimite_retornaStatus400() throws Exception {
            var nomeLongo = "A".repeat(51);
            var requisicao = new CategoriaDoacaoDTO.Requisicao(nomeLongo, "Descrição");

            mockMvc.perform(
                            post("/doacoes/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 409 quando o nome já estiver cadastrado (RN03)")
        void cadastrar_comNomeDuplicado_retornaStatus409() throws Exception {
            categoriaDoacaoRepository.save(new CategoriaDoacao("Vestuário", "Roupas em geral"));

            var requisicao = new CategoriaDoacaoDTO.Requisicao("vestuário", "Outra descrição");

            mockMvc.perform(
                            post("/doacoes/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/categorias - Listagem Paginada")
    class ListarPaginado {

        @Test
        @DisplayName("Deve retornar listagem paginada de categorias de doação com status 200")
        void listar_comPaginacao_retornaStatus200EListaPaginada() throws Exception {
            categoriaDoacaoRepository.save(new CategoriaDoacao("Alimentos", "Desc 1"));
            categoriaDoacaoRepository.save(new CategoriaDoacao("Roupas", "Desc 2"));

            mockMvc.perform(get("/doacoes/categorias").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados").isArray())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }

        @Test
        @DisplayName("Deve filtrar inativos por padrão na listagem")
        void listar_comFiltroIncluirInativos_retornaStatus200() throws Exception {
            categoriaDoacaoRepository.save(new CategoriaDoacao("Categoria Ativa", "Desc"));
            CategoriaDoacao inativa = new CategoriaDoacao("Categoria Inativa", "Desc");
            inativa.desativar();
            categoriaDoacaoRepository.save(inativa);

            mockMvc.perform(get("/doacoes/categorias").param("incluirInativos", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Categoria Ativa"));

            mockMvc.perform(get("/doacoes/categorias").param("incluirInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }

        @Test
        @DisplayName("Deve filtrar por nome e descrição")
        void listar_comFiltrosNomeEDescricao_retornaItensFiltrados() throws Exception {
            categoriaDoacaoRepository.save(new CategoriaDoacao("Cadeiras", "Móveis para escritório"));
            categoriaDoacaoRepository.save(new CategoriaDoacao("Mesas", "Móveis para jantar"));

            mockMvc.perform(
                            get("/doacoes/categorias")
                                    .param("nome", "Cadeiras")
                                    .param("descricao", "escritório"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Cadeiras"));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/categorias/todas - Listagem Não Paginada para Dropdowns")
    class ListarTodas {

        @Test
        @DisplayName("Deve retornar lista completa de categorias de doação ativas com status 200")
        void listarTodas_retornaStatus200EListaCompleta() throws Exception {
            categoriaDoacaoRepository.save(new CategoriaDoacao("Categoria A", "Desc A"));
            categoriaDoacaoRepository.save(new CategoriaDoacao("Categoria B", "Desc B"));

            mockMvc.perform(get("/doacoes/categorias/todas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/categorias/{id} - Detalhamento")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes da categoria de doação com status 200 quando existir")
        void buscarPorId_comIdExistente_retornaStatus200() throws Exception {
            CategoriaDoacao cat =
                    categoriaDoacaoRepository.save(new CategoriaDoacao("Eletrônicos", "TVs e computadores"));

            mockMvc.perform(get("/doacoes/categorias/{id}", cat.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cat.getId()))
                    .andExpect(jsonPath("$.nome").value("Eletrônicos"))
                    .andExpect(jsonPath("$.descricao").value("TVs e computadores"))
                    .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @DisplayName("Deve retornar status 404 quando o ID não existir ou estiver inativo")
        void buscarPorId_comIdInexistenteOuInativo_retornaStatus404() throws Exception {
            mockMvc.perform(get("/doacoes/categorias/{id}", 99999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /doacoes/categorias/{id} - Atualização")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar dados da categoria de doação com status 200")
        void atualizar_comDadosValidos_retornaStatus200() throws Exception {
            CategoriaDoacao cat =
                    categoriaDoacaoRepository.save(new CategoriaDoacao("Brinquedos", "Para crianças"));

            var requisicao =
                    new CategoriaDoacaoDTO.Atualizacao("Brinquedos Infantis", "Para crianças e bebês");

            mockMvc.perform(
                            put("/doacoes/categorias/{id}", cat.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cat.getId()))
                    .andExpect(jsonPath("$.nome").value("Brinquedos Infantis"))
                    .andExpect(jsonPath("$.descricao").value("Para crianças e bebês"));
        }

        @Test
        @DisplayName("Deve retornar status 409 ao tentar atualizar para nome já existente")
        void atualizar_comNomeDuplicado_retornaStatus409() throws Exception {
            categoriaDoacaoRepository.save(new CategoriaDoacao("Item 1", "Desc 1"));
            CategoriaDoacao cat2 =
                    categoriaDoacaoRepository.save(new CategoriaDoacao("Item 2", "Desc 2"));

            var requisicao = new CategoriaDoacaoDTO.Atualizacao("item 1", "Desc Atualizada");

            mockMvc.perform(
                            put("/doacoes/categorias/{id}", cat2.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar categoria inexistente")
        void atualizar_comIdInexistente_retornaStatus404() throws Exception {
            var requisicao = new CategoriaDoacaoDTO.Atualizacao("Nome", "Desc");

            mockMvc.perform(
                            put("/doacoes/categorias/{id}", 99999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/categorias/{id}/desativar - Desativação Lógica")
    class Desativar {

        @Test
        @DisplayName("Deve desativar categoria com sucesso retornando status 204")
        void desativar_comIdExistente_retornaStatus204() throws Exception {
            CategoriaDoacao cat =
                    categoriaDoacaoRepository.save(new CategoriaDoacao("Categoria Para Desativar", "Desc"));

            mockMvc.perform(patch("/doacoes/categorias/{id}/desativar", cat.getId()))
                    .andExpect(status().isNoContent());

            // Garantir que a busca ativa agora retorna 404
            mockMvc.perform(get("/doacoes/categorias/{id}", cat.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar desativar categoria inexistente ou já inativa")
        void desativar_comIdInexistenteOuJaInativo_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/doacoes/categorias/{id}/desativar", 99999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/categorias/{id}/reativar - Reativação")
    class Reativar {

        @Test
        @DisplayName("Deve reativar categoria com sucesso retornando status 204")
        void reativar_comIdExistente_retornaStatus204() throws Exception {
            CategoriaDoacao cat = new CategoriaDoacao("Categoria Para Reativar", "Desc");
            cat.desativar();
            cat = categoriaDoacaoRepository.save(cat);

            mockMvc.perform(patch("/doacoes/categorias/{id}/reativar", cat.getId()))
                    .andExpect(status().isNoContent());

            // Garantir que a busca ativa agora retorna 200
            mockMvc.perform(get("/doacoes/categorias/{id}", cat.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar reativar categoria inexistente")
        void reativar_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/doacoes/categorias/{id}/reativar", 99999L))
                    .andExpect(status().isNotFound());
        }
    }
}
