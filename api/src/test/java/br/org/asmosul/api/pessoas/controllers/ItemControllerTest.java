package br.org.asmosul.api.pessoas.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.pessoas.dtos.CategoriaDTO;
import br.org.asmosul.api.pessoas.dtos.ItemDTO;
import br.org.asmosul.api.pessoas.models.UnidadeMedida;
import br.org.asmosul.api.pessoas.services.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("Testes do ItemController")
class ItemControllerTest extends BaseAPITest {

    private static final String URL_BASE = "/itens";

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private ItemService itemService;

    private CategoriaDTO.Resumo criarCategoriaResumoExemplo() {
        return new CategoriaDTO.Resumo(1L, "Alimentos", "Itens alimentícios", true);
    }

    private ItemDTO.Detalhe criarDetalheExemplo(Long id, String nome) {
        return new ItemDTO.Detalhe(
                id,
                nome,
                criarCategoriaResumoExemplo(),
                10.0,
                8.50,
                UnidadeMedida.KG,
                "Descrição " + nome,
                true);
    }

    private ItemDTO.Resumo criarResumoExemplo(Long id, String nome) {
        return new ItemDTO.Resumo(
                id,
                nome,
                "Alimentos",
                10.0,
                8.50,
                UnidadeMedida.KG,
                "Descrição " + nome,
                true);
    }

    @Nested
    @DisplayName("POST /itens - Cadastrar item")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar um item com dados válidos e retornar 201")
        void cadastrarItem_comDadosValidos_retorna201ECorpo() throws Exception {
            var requisicao =
                    new ItemDTO.Requisicao(
                            "Feijão Carioca 1kg", 1L, 8.50, UnidadeMedida.KG, "Feijão carioca tipo 1");
            var detalhe = criarDetalheExemplo(10L, "Feijão Carioca 1kg");

            when(itemService.cadastrar(any(ItemDTO.Requisicao.class))).thenReturn(detalhe);

            mockMvc.perform(
                            post(URL_BASE)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/itens/10")))
                    .andExpect(jsonPath("$.id").value(10L))
                    .andExpect(jsonPath("$.nome").value("Feijão Carioca 1kg"));
        }

        @Test
        @DisplayName("Deve retornar 400 ao cadastrar item sem campos obrigatórios")
        void cadastrarItem_semCamposObrigatorios_retorna400() throws Exception {
            var requisicao = new ItemDTO.Requisicao("", null, null, null, "");

            mockMvc.perform(
                            post(URL_BASE)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /itens/{id} - Atualizar item")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar um item existente e retornar 200")
        void atualizarItem_comDadosValidos_retorna200() throws Exception {
            var atualizacao =
                    new ItemDTO.Atualizacao(
                            "Óleo de Soja 900ml", 1L, 15.75, UnidadeMedida.LT, "Descrição");
            var detalhe = criarDetalheExemplo(1L, "Óleo de Soja 900ml");

            when(itemService.atualizar(eq(1L), any(ItemDTO.Atualizacao.class))).thenReturn(detalhe);

            mockMvc.perform(
                            put(URL_BASE + "/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(atualizacao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Óleo de Soja 900ml"));
        }

        @Test
        @DisplayName("Deve retornar 404 ao tentar atualizar item inexistente")
        void atualizarItem_comIdInexistente_retorna404() throws Exception {
            var atualizacao =
                    new ItemDTO.Atualizacao("Inexistente", 1L, 12.0, UnidadeMedida.UN, "Descrição");

            when(itemService.atualizar(eq(9999L), any(ItemDTO.Atualizacao.class)))
                    .thenThrow(new EntidadeNaoEncontradaException("Item não encontrado."));

            mockMvc.perform(
                            put(URL_BASE + "/{id}", 9999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(atualizacao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /itens/{id} - Buscar por id")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar item detalhado quando o ID existir")
        void buscarPorId_existente_retorna200() throws Exception {
            var detalhe = criarDetalheExemplo(1L, "Macarrão 500g");

            when(itemService.buscarPorId(1L)).thenReturn(detalhe);

            mockMvc.perform(get(URL_BASE + "/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Macarrão 500g"));
        }

        @Test
        @DisplayName("Deve retornar 404 quando o ID não existir")
        void buscarPorId_inexistente_retorna404() throws Exception {
            when(itemService.buscarPorId(9999L))
                    .thenThrow(new EntidadeNaoEncontradaException("Item não encontrado."));

            mockMvc.perform(get(URL_BASE + "/{id}", 9999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /itens - Listar e filtrar")
    class Listar {

        @Test
        @DisplayName("Deve retornar lista paginada de itens")
        void listarItens_retorna200EPagina() throws Exception {
            var item1 = criarResumoExemplo(1L, "Leite Integral 1L");
            var item2 = criarResumoExemplo(2L, "Açúcar 1kg");
            var respostaPaginada = new RespostaPaginada<>(List.of(item1, item2), 0, 10, 2L, 1);

            when(itemService.listar(any(), any(Pageable.class), eq(false))).thenReturn(respostaPaginada);

            mockMvc.perform(get(URL_BASE).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados", hasSize(2)))
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }
    }

    @Nested
    @DisplayName("PATCH /itens/{id} - Desativar e reativar")
    class DesativarEReativar {

        @Test
        @DisplayName("Deve desativar item com sucesso")
        void desativarItem_sucesso() throws Exception {
            doNothing().when(itemService).inativar(1L);

            mockMvc.perform(patch(URL_BASE + "/{id}/desativar", 1L))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Deve reativar item com sucesso")
        void reativarItem_sucesso() throws Exception {
            doNothing().when(itemService).reativar(1L);

            mockMvc.perform(patch(URL_BASE + "/{id}/reativar", 1L))
                    .andExpect(status().isNoContent());
        }
    }
}
