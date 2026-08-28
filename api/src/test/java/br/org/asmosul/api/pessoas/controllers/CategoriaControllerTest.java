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
import br.org.asmosul.api.pessoas.dtos.CategoriaDTO;
import br.org.asmosul.api.pessoas.services.CategoriaService;
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

@WebMvcTest(CategoriaController.class)
@DisplayName("Testes Unitários - CategoriaController")
class CategoriaControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private CategoriaService categoriaService;

    @Nested
    @DisplayName("POST /categorias - Cadastro de Categoria")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar categoria com sucesso retornando status 201 e Location header")
        void deveCadastrarCategoriaComSucesso() throws Exception {
            var requisicao =
                    new CategoriaDTO.Requisicao("Sócio Fundador", "Categoria de fundadores");
            var detalheRetornado =
                    new CategoriaDTO.Detalhe(1L, "Sócio Fundador", "Categoria de fundadores");

            when(categoriaService.cadastrar(any(CategoriaDTO.Requisicao.class)))
                    .thenReturn(detalheRetornado);

            mockMvc.perform(
                            post("/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/categorias/1")))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Sócio Fundador"))
                    .andExpect(jsonPath("$.descricao").value("Categoria de fundadores"));

            verify(categoriaService).cadastrar(any(CategoriaDTO.Requisicao.class));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome estiver em branco")
        void deveRetornar400AoCadastrarComNomeEmBranco() throws Exception {
            var requisicao = new CategoriaDTO.Requisicao("", "Descrição válida");

            mockMvc.perform(
                            post("/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome exceder 50 caracteres")
        void deveRetornar400AoCadastrarComNomeExcedendoLimite() throws Exception {
            var nomeLongo = "A".repeat(51);
            var requisicao = new CategoriaDTO.Requisicao(nomeLongo, "Descrição");

            mockMvc.perform(
                            post("/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 409 quando o serviço lançar ConflitoDadosException")
        void deveRetornar409AoCadastrarComNomeDuplicado() throws Exception {
            var requisicao = new CategoriaDTO.Requisicao("Sócio Efetivo", "Descrição");

            when(categoriaService.cadastrar(any(CategoriaDTO.Requisicao.class)))
                    .thenThrow(
                            new ConflitoDadosException(
                                    "Já existe uma categoria cadastrada com este nome."));

            mockMvc.perform(
                            post("/categorias")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"))
                    .andExpect(
                            jsonPath("$.detail")
                                    .value("Já existe uma categoria cadastrada com este nome."));
        }
    }

    @Nested
    @DisplayName("GET /categorias - Listagem Paginada")
    class ListarPaginado {

        @Test
        @DisplayName("Deve retornar listagem paginada de categorias com status 200")
        void deveListarCategoriasPaginadas() throws Exception {
            var resumo = new CategoriaDTO.Resumo(1L, "Sócio Fundador", "Descrição", true);
            var respostaPaginada = new RespostaPaginada<>(List.of(resumo), 0, 10, 1L, 1);

            when(categoriaService.listar(any(Pageable.class), eq(false)))
                    .thenReturn(respostaPaginada);

            mockMvc.perform(get("/categorias").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].id").value(1L))
                    .andExpect(jsonPath("$.dados[0].nome").value("Sócio Fundador"))
                    .andExpect(jsonPath("$.dados[0].ativo").value(true))
                    .andExpect(jsonPath("$.paginaAtual").value(0))
                    .andExpect(jsonPath("$.tamanhoPagina").value(10))
                    .andExpect(jsonPath("$.totalElementos").value(1L))
                    .andExpect(jsonPath("$.totalPaginas").value(1));

            verify(categoriaService).listar(any(Pageable.class), eq(false));
        }

        @Test
        @DisplayName("Deve repassar o parâmetro incluirInativos para o serviço")
        void deveListarCategoriasComParametroIncluirInativos() throws Exception {
            var respostaPaginada =
                    new RespostaPaginada<CategoriaDTO.Resumo>(List.of(), 0, 10, 0L, 0);

            when(categoriaService.listar(any(Pageable.class), eq(true)))
                    .thenReturn(respostaPaginada);

            mockMvc.perform(get("/categorias").param("incluirInativos", "true"))
                    .andExpect(status().isOk());

            verify(categoriaService).listar(any(Pageable.class), eq(true));
        }
    }

    @Nested
    @DisplayName("GET /categorias/todas - Listagem Completa Não Paginada")
    class ListarTodas {

        @Test
        @DisplayName("Deve retornar todas as categorias em lista simples com status 200")
        void deveListarTodasAsCategorias() throws Exception {
            var lista =
                    List.of(
                            new CategoriaDTO.Resumo(1L, "Categoria 1", "Desc 1", true),
                            new CategoriaDTO.Resumo(2L, "Categoria 2", "Desc 2", true));

            when(categoriaService.listarTodas(false)).thenReturn(lista);

            mockMvc.perform(get("/categorias/todas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].nome").value("Categoria 1"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].nome").value("Categoria 2"));

            verify(categoriaService).listarTodas(false);
        }

        @Test
        @DisplayName("Deve repassar incluirInativos ao buscar todas as categorias")
        void deveListarTodasComParametroIncluirInativos() throws Exception {
            when(categoriaService.listarTodas(true)).thenReturn(List.of());

            mockMvc.perform(get("/categorias/todas").param("incluirInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(categoriaService).listarTodas(true);
        }
    }

    @Nested
    @DisplayName("GET /categorias/{id} - Busca por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes da categoria quando encontrada com status 200")
        void deveBuscarCategoriaPorIdComSucesso() throws Exception {
            var detalhe = new CategoriaDTO.Detalhe(1L, "Sócio Fundador", "Descrição");

            when(categoriaService.buscarPorId(1L)).thenReturn(detalhe);

            mockMvc.perform(get("/categorias/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Sócio Fundador"))
                    .andExpect(jsonPath("$.descricao").value("Descrição"));

            verify(categoriaService).buscarPorId(1L);
        }

        @Test
        @DisplayName(
                "Deve retornar status 404 quando o serviço lançar EntidadeNaoEncontradaException")
        void deveRetornar404AoBuscarCategoriaInexistente() throws Exception {
            when(categoriaService.buscarPorId(99L))
                    .thenThrow(
                            new EntidadeNaoEncontradaException(
                                    "Categoria ativa não encontrada com o ID informado: 99"));

            mockMvc.perform(get("/categorias/{id}", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                    .andExpect(
                            jsonPath("$.detail")
                                    .value(
                                            "Categoria ativa não encontrada com o ID informado: 99"));
        }
    }

    @Nested
    @DisplayName("PUT /categorias/{id} - Atualização")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar categoria com sucesso retornando status 200")
        void deveAtualizarCategoriaComSucesso() throws Exception {
            var requisicao = new CategoriaDTO.Atualizacao("Sócio Atualizado", "Nova Descrição");
            var detalhe = new CategoriaDTO.Detalhe(1L, "Sócio Atualizado", "Nova Descrição");

            when(categoriaService.atualizar(eq(1L), any(CategoriaDTO.Atualizacao.class)))
                    .thenReturn(detalhe);

            mockMvc.perform(
                            put("/categorias/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Sócio Atualizado"))
                    .andExpect(jsonPath("$.descricao").value("Nova Descrição"));

            verify(categoriaService).atualizar(eq(1L), any(CategoriaDTO.Atualizacao.class));
        }

        @Test
        @DisplayName("Deve retornar status 400 ao atualizar com nome em branco")
        void deveRetornar400AoAtualizarComNomeEmBranco() throws Exception {
            var requisicao = new CategoriaDTO.Atualizacao("", "Nova Descrição");

            mockMvc.perform(
                            put("/categorias/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar categoria inexistente")
        void deveRetornar404AoAtualizarCategoriaInexistente() throws Exception {
            var requisicao = new CategoriaDTO.Atualizacao("Nome", "Descrição");

            when(categoriaService.atualizar(eq(99L), any(CategoriaDTO.Atualizacao.class)))
                    .thenThrow(
                            new EntidadeNaoEncontradaException(
                                    "Categoria ativa não encontrada com o ID informado: 99"));

            mockMvc.perform(
                            put("/categorias/{id}", 99L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar status 409 ao atualizar para nome já existente")
        void deveRetornar409AoAtualizarParaNomeDuplicado() throws Exception {
            var requisicao = new CategoriaDTO.Atualizacao("Nome Conflitante", "Descrição");

            when(categoriaService.atualizar(eq(1L), any(CategoriaDTO.Atualizacao.class)))
                    .thenThrow(
                            new ConflitoDadosException(
                                    "Já existe uma categoria cadastrada com este nome."));

            mockMvc.perform(
                            put("/categorias/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PATCH /categorias/{id}/desativar - Desativação (Soft Delete)")
    class Desativar {

        @Test
        @DisplayName("Deve desativar categoria com sucesso retornando status 204")
        void deveDesativarCategoriaComSucesso() throws Exception {
            doNothing().when(categoriaService).desativar(1L);

            mockMvc.perform(patch("/categorias/{id}/desativar", 1L))
                    .andExpect(status().isNoContent());

            verify(categoriaService).desativar(1L);
        }

        @Test
        @DisplayName(
                "Deve retornar status 404 ao tentar desativar categoria inexistente ou já inativa")
        void deveRetornar404AoDesativarCategoriaInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Categoria ativa não encontrada com o ID informado: 99"))
                    .when(categoriaService)
                    .desativar(99L);

            mockMvc.perform(patch("/categorias/{id}/desativar", 99L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /categorias/{id}/reativar - Reativação")
    class Reativar {

        @Test
        @DisplayName("Deve reativar categoria com sucesso retornando status 204")
        void deveReativarCategoriaComSucesso() throws Exception {
            doNothing().when(categoriaService).reativar(1L);

            mockMvc.perform(patch("/categorias/{id}/reativar", 1L))
                    .andExpect(status().isNoContent());

            verify(categoriaService).reativar(1L);
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar reativar categoria inexistente")
        void deveRetornar404AoReativarCategoriaInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Categoria não encontrada com o ID informado: 99"))
                    .when(categoriaService)
                    .reativar(99L);

            mockMvc.perform(patch("/categorias/{id}/reativar", 99L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /categorias/{id} - Exclusão Física")
    class Excluir {

        @Test
        @DisplayName("Deve excluir categoria com sucesso retornando status 204")
        void deveExcluirCategoriaComSucesso() throws Exception {
            doNothing().when(categoriaService).excluir(1L);

            mockMvc.perform(delete("/categorias/{id}", 1L)).andExpect(status().isNoContent());

            verify(categoriaService).excluir(1L);
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar excluir categoria inexistente")
        void deveRetornar404AoExcluirCategoriaInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Categoria não encontrada com o ID informado: 99"))
                    .when(categoriaService)
                    .excluir(99L);

            mockMvc.perform(delete("/categorias/{id}", 99L)).andExpect(status().isNotFound());
        }
    }
}
