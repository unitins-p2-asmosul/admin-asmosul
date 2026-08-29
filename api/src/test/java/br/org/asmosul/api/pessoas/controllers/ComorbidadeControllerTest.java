package br.org.asmosul.api.pessoas.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.pessoas.dtos.ComorbidadeDTO;
import br.org.asmosul.api.pessoas.services.ComorbidadeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ComorbidadeController.class)
@DisplayName("Testes Unitários - ComorbidadeController")
class ComorbidadeControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private ComorbidadeService comorbidadeService;

    @Nested
    @DisplayName("POST /comorbidades - Cadastro de Comorbidade")
    class Cadastrar {

        @Test
        @DisplayName(
                "Deve cadastrar comorbidade com sucesso retornando status 201 e Location header")
        void deveCadastrarComorbidadeComSucesso() throws Exception {
            var requisicao = new ComorbidadeDTO.Requisicao("Hipertensão", "Pressão alta crônica");
            var detalheRetornado =
                    new ComorbidadeDTO.Detalhe(1L, "Hipertensão", "Pressão alta crônica");

            when(comorbidadeService.cadastrar(any(ComorbidadeDTO.Requisicao.class)))
                    .thenReturn(detalheRetornado);

            mockMvc.perform(
                            post("/comorbidades")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/comorbidades/1")))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Hipertensão"))
                    .andExpect(jsonPath("$.descricao").value("Pressão alta crônica"));

            verify(comorbidadeService).cadastrar(any(ComorbidadeDTO.Requisicao.class));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome for nulo ou em branco")
        void deveRetornar400AoCadastrarComNomeEmBranco() throws Exception {
            var requisicao = new ComorbidadeDTO.Requisicao("", "Descrição válida");

            mockMvc.perform(
                            post("/comorbidades")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome exceder 50 caracteres")
        void deveRetornar400AoCadastrarComNomeExcedendoLimite() throws Exception {
            var nomeLongo = "H".repeat(51);
            var requisicao = new ComorbidadeDTO.Requisicao(nomeLongo, "Descrição");

            mockMvc.perform(
                            post("/comorbidades")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 409 quando o serviço lançar ConflitoDadosException")
        void deveRetornar409AoCadastrarComNomeDuplicado() throws Exception {
            var requisicao = new ComorbidadeDTO.Requisicao("Diabetes", "Tipo 2");

            when(comorbidadeService.cadastrar(any(ComorbidadeDTO.Requisicao.class)))
                    .thenThrow(
                            new ConflitoDadosException(
                                    "Já existe uma comorbidade cadastrada com este nome."));

            mockMvc.perform(
                            post("/comorbidades")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"))
                    .andExpect(
                            jsonPath("$.detail")
                                    .value("Já existe uma comorbidade cadastrada com este nome."));
        }
    }

    @Nested
    @DisplayName("GET /comorbidades - Listagem Paginada")
    class ListarPaginado {

        @Test
        @DisplayName("Deve retornar listagem paginada de comorbidades com status 200")
        void deveListarComorbidadesPaginadas() throws Exception {
            var resumo = new ComorbidadeDTO.Resumo(1L, "Hipertensão", "Descrição", true);
            var respostaPaginada = new RespostaPaginada<>(List.of(resumo), 0, 10, 1L, 1);

            when(comorbidadeService.listar(any(Pageable.class), eq(false)))
                    .thenReturn(respostaPaginada);

            mockMvc.perform(get("/comorbidades").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].id").value(1L))
                    .andExpect(jsonPath("$.dados[0].nome").value("Hipertensão"))
                    .andExpect(jsonPath("$.dados[0].ativo").value(true))
                    .andExpect(jsonPath("$.paginaAtual").value(0))
                    .andExpect(jsonPath("$.tamanhoPagina").value(10))
                    .andExpect(jsonPath("$.totalElementos").value(1L))
                    .andExpect(jsonPath("$.totalPaginas").value(1));

            verify(comorbidadeService).listar(any(Pageable.class), eq(false));
        }

        @Test
        @DisplayName("Deve repassar o parâmetro incluirInativos para o serviço")
        void deveListarComorbidadesComParametroIncluirInativos() throws Exception {
            var respostaPaginada =
                    new RespostaPaginada<ComorbidadeDTO.Resumo>(List.of(), 0, 10, 0L, 0);

            when(comorbidadeService.listar(any(Pageable.class), eq(true)))
                    .thenReturn(respostaPaginada);

            mockMvc.perform(get("/comorbidades").param("incluirInativos", "true"))
                    .andExpect(status().isOk());

            verify(comorbidadeService).listar(any(Pageable.class), eq(true));
        }
    }

    @Nested
    @DisplayName("GET /comorbidades/todas - Listagem Completa Não Paginada")
    class ListarTodas {

        @Test
        @DisplayName("Deve retornar todas as comorbidades em lista simples com status 200")
        void deveListarTodasAsComorbidades() throws Exception {
            var lista =
                    List.of(
                            new ComorbidadeDTO.Resumo(1L, "Hipertensão", "Desc 1", true),
                            new ComorbidadeDTO.Resumo(2L, "Diabetes", "Desc 2", true));

            when(comorbidadeService.listarTodas(false)).thenReturn(lista);

            mockMvc.perform(get("/comorbidades/todas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].nome").value("Hipertensão"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].nome").value("Diabetes"));

            verify(comorbidadeService).listarTodas(false);
        }

        @Test
        @DisplayName("Deve repassar incluirInativos ao buscar todas as comorbidades")
        void deveListarTodasComParametroIncluirInativos() throws Exception {
            when(comorbidadeService.listarTodas(true)).thenReturn(List.of());

            mockMvc.perform(get("/comorbidades/todas").param("incluirInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(comorbidadeService).listarTodas(true);
        }
    }

    @Nested
    @DisplayName("GET /comorbidades/{id} - Busca por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes da comorbidade quando encontrada com status 200")
        void deveBuscarComorbidadePorIdComSucesso() throws Exception {
            var detalhe = new ComorbidadeDTO.Detalhe(1L, "Hipertensão", "Descrição");

            when(comorbidadeService.buscarPorId(1L)).thenReturn(detalhe);

            mockMvc.perform(get("/comorbidades/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Hipertensão"))
                    .andExpect(jsonPath("$.descricao").value("Descrição"));

            verify(comorbidadeService).buscarPorId(1L);
        }

        @Test
        @DisplayName(
                "Deve retornar status 404 quando o serviço lançar EntidadeNaoEncontradaException")
        void deveRetornar404AoBuscarComorbidadeInexistente() throws Exception {
            when(comorbidadeService.buscarPorId(99L))
                    .thenThrow(
                            new EntidadeNaoEncontradaException(
                                    "Comorbidade ativa não encontrada com o ID informado: 99"));

            mockMvc.perform(get("/comorbidades/{id}", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                    .andExpect(
                            jsonPath("$.detail")
                                    .value(
                                            "Comorbidade ativa não encontrada com o ID informado: 99"));
        }
    }

    @Nested
    @DisplayName("PUT /comorbidades/{id} - Atualização")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar comorbidade com sucesso retornando status 200")
        void deveAtualizarComorbidadeComSucesso() throws Exception {
            var requisicao = new ComorbidadeDTO.Atualizacao("Hipertensão Severa", "Nova Descrição");
            var detalhe = new ComorbidadeDTO.Detalhe(1L, "Hipertensão Severa", "Nova Descrição");

            when(comorbidadeService.atualizar(eq(1L), any(ComorbidadeDTO.Atualizacao.class)))
                    .thenReturn(detalhe);

            mockMvc.perform(
                            put("/comorbidades/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Hipertensão Severa"))
                    .andExpect(jsonPath("$.descricao").value("Nova Descrição"));

            verify(comorbidadeService).atualizar(eq(1L), any(ComorbidadeDTO.Atualizacao.class));
        }

        @Test
        @DisplayName("Deve retornar status 400 ao atualizar com nome em branco")
        void deveRetornar400AoAtualizarComNomeEmBranco() throws Exception {
            var requisicao = new ComorbidadeDTO.Atualizacao("", "Nova Descrição");

            mockMvc.perform(
                            put("/comorbidades/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar comorbidade inexistente")
        void deveRetornar404AoAtualizarComorbidadeInexistente() throws Exception {
            var requisicao = new ComorbidadeDTO.Atualizacao("Nome", "Descrição");

            when(comorbidadeService.atualizar(eq(99L), any(ComorbidadeDTO.Atualizacao.class)))
                    .thenThrow(
                            new EntidadeNaoEncontradaException(
                                    "Comorbidade ativa não encontrada com o ID informado: 99"));

            mockMvc.perform(
                            put("/comorbidades/{id}", 99L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar status 409 ao atualizar para nome já existente")
        void deveRetornar409AoAtualizarParaNomeDuplicado() throws Exception {
            var requisicao = new ComorbidadeDTO.Atualizacao("Diabetes", "Descrição");

            when(comorbidadeService.atualizar(eq(1L), any(ComorbidadeDTO.Atualizacao.class)))
                    .thenThrow(
                            new ConflitoDadosException(
                                    "Já existe uma comorbidade cadastrada com este nome."));

            mockMvc.perform(
                            put("/comorbidades/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PATCH /comorbidades/{id}/desativar - Desativação (Soft Delete)")
    class Desativar {

        @Test
        @DisplayName("Deve desativar comorbidade com sucesso retornando status 204")
        void deveDesativarComorbidadeComSucesso() throws Exception {
            doNothing().when(comorbidadeService).desativar(1L);

            mockMvc.perform(patch("/comorbidades/{id}/desativar", 1L))
                    .andExpect(status().isNoContent());

            verify(comorbidadeService).desativar(1L);
        }

        @Test
        @DisplayName(
                "Deve retornar status 404 ao tentar desativar comorbidade inexistente ou já inativa")
        void deveRetornar404AoDesativarComorbidadeInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Comorbidade ativa não encontrada com o ID informado: 99"))
                    .when(comorbidadeService)
                    .desativar(99L);

            mockMvc.perform(patch("/comorbidades/{id}/desativar", 99L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /comorbidades/{id}/reativar - Reativação")
    class Reativar {

        @Test
        @DisplayName("Deve reativar comorbidade com sucesso retornando status 204")
        void deveReativarComorbidadeComSucesso() throws Exception {
            doNothing().when(comorbidadeService).reativar(1L);

            mockMvc.perform(patch("/comorbidades/{id}/reativar", 1L))
                    .andExpect(status().isNoContent());

            verify(comorbidadeService).reativar(1L);
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar reativar comorbidade inexistente")
        void deveRetornar404AoReativarComorbidadeInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Comorbidade não encontrada com o ID informado: 99"))
                    .when(comorbidadeService)
                    .reativar(99L);

            mockMvc.perform(patch("/comorbidades/{id}/reativar", 99L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /comorbidades/{id} - Exclusão Física")
    class Excluir {

        @Test
        @DisplayName("Deve excluir comorbidade com sucesso retornando status 204")
        void deveExcluirComorbidadeComSucesso() throws Exception {
            doNothing().when(comorbidadeService).excluir(1L);

            mockMvc.perform(delete("/comorbidades/{id}", 1L)).andExpect(status().isNoContent());

            verify(comorbidadeService).excluir(1L);
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar excluir comorbidade inexistente")
        void deveRetornar404AoExcluirComorbidadeInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Comorbidade não encontrada com o ID informado: 99"))
                    .when(comorbidadeService)
                    .excluir(99L);

            mockMvc.perform(delete("/comorbidades/{id}", 99L)).andExpect(status().isNotFound());
        }
    }
}
