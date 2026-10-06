package br.org.asmosul.api.doacoes.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.doacoes.dtos.EnderecoDoacaoDTO;
import br.org.asmosul.api.doacoes.models.EnderecoDoacao;
import br.org.asmosul.api.doacoes.repositories.EnderecoDoacaoRepository;
import br.org.asmosul.api.pessoas.models.Uf;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("Testes de Integração - EnderecoDoacaoController")
class EnderecoDoacaoControllerTest extends BaseAPITest {

    @Autowired private MockMvc mockMvc;

    @MockitoSpyBean private EnderecoDoacaoRepository enderecoDoacaoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        enderecoDoacaoRepository.deleteAll();
    }

    private EnderecoDoacao criarEnderecoExemplo(String nome) {
        return enderecoDoacaoRepository.save(
                new EnderecoDoacao(
                        nome,
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Centro",
                        "Av. JK",
                        "100",
                        "Sala 1",
                        "Depósito principal"));
    }

    @Nested
    @DisplayName("POST /doacoes/enderecos - Cadastro")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar endereço com sucesso retornando status 201 e Location header")
        void cadastrar_comDadosValidos_retornaStatus201ELocationHeader() throws Exception {
            var requisicao =
                    new EnderecoDoacaoDTO.Requisicao(
                            "Depósito Norte",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            "Sala 1",
                            "Galpão de triagem");

            mockMvc.perform(
                            post("/doacoes/enderecos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            org.hamcrest.Matchers.matchesPattern(
                                                    ".*/doacoes/enderecos/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("Depósito Norte"))
                    .andExpect(jsonPath("$.cidade").value("Palmas"))
                    .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome estiver em branco")
        void cadastrar_comNomeEmBranco_retornaStatus400() throws Exception {
            var requisicao =
                    new EnderecoDoacaoDTO.Requisicao(
                            "",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);

            mockMvc.perform(
                            post("/doacoes/enderecos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o CEP estiver em branco")
        void cadastrar_comCepEmBranco_retornaStatus400() throws Exception {
            var requisicao =
                    new EnderecoDoacaoDTO.Requisicao(
                            "Depósito Teste",
                            "",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);

            mockMvc.perform(
                            post("/doacoes/enderecos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 409 quando o nome já estiver cadastrado (RN03)")
        void cadastrar_comNomeDuplicado_retornaStatus409() throws Exception {
            criarEnderecoExemplo("Depósito Existente");

            var requisicao =
                    new EnderecoDoacaoDTO.Requisicao(
                            "depósito existente",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "200",
                            null,
                            null);

            mockMvc.perform(
                            post("/doacoes/enderecos")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/enderecos - Listagem Paginada")
    class ListarPaginado {

        @Test
        @DisplayName("Deve retornar listagem paginada de endereços com status 200")
        void listar_comPaginacao_retornaStatus200EListaPaginada() throws Exception {
            criarEnderecoExemplo("Depósito A");
            criarEnderecoExemplo("Depósito B");

            mockMvc.perform(get("/doacoes/enderecos").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados").isArray())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }

        @Test
        @DisplayName("Deve filtrar inativos por padrão na listagem")
        void listar_comFiltroIncluirInativos_retornaStatus200() throws Exception {
            criarEnderecoExemplo("Ativo");
            EnderecoDoacao inativo = criarEnderecoExemplo("Inativo");
            inativo.desativar();
            enderecoDoacaoRepository.save(inativo);

            mockMvc.perform(get("/doacoes/enderecos").param("incluirInativos", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Ativo"));

            mockMvc.perform(get("/doacoes/enderecos").param("incluirInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }

        @Test
        @DisplayName("Deve filtrar por cidade, bairro e nome")
        void listar_comFiltrosMultiplos_retornaItensFiltrados() throws Exception {
            criarEnderecoExemplo("Depósito Central");

            mockMvc.perform(
                            get("/doacoes/enderecos")
                                    .param("nome", "Central")
                                    .param("cidade", "Palmas")
                                    .param("bairro", "Centro"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Depósito Central"));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/enderecos/todas - Listagem Não Paginada para Dropdowns")
    class ListarTodas {

        @Test
        @DisplayName("Deve retornar lista completa de endereços ativos com status 200")
        void listarTodas_retornaStatus200EListaCompleta() throws Exception {
            criarEnderecoExemplo("Depósito 1");
            criarEnderecoExemplo("Depósito 2");

            mockMvc.perform(get("/doacoes/enderecos/todas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/enderecos/{id} - Detalhamento")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes do endereço com status 200 quando existir")
        void buscarPorId_comIdExistente_retornaStatus200() throws Exception {
            EnderecoDoacao endereco = criarEnderecoExemplo("Depósito Detalhe");

            mockMvc.perform(get("/doacoes/enderecos/{id}", endereco.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(endereco.getId()))
                    .andExpect(jsonPath("$.nome").value("Depósito Detalhe"))
                    .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @DisplayName("Deve retornar status 404 quando o ID não existir ou estiver inativo")
        void buscarPorId_comIdInexistenteOuInativo_retornaStatus404() throws Exception {
            mockMvc.perform(get("/doacoes/enderecos/{id}", 99999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /doacoes/enderecos/{id} - Atualização")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar dados do endereço com status 200")
        void atualizar_comDadosValidos_retornaStatus200() throws Exception {
            EnderecoDoacao endereco = criarEnderecoExemplo("Depósito Antigo");

            var requisicao =
                    new EnderecoDoacaoDTO.Atualizacao(
                            "Depósito Atualizado",
                            "77000-222",
                            Uf.TO,
                            "Palmas",
                            "Sul",
                            "Av. Teotônio",
                            "500",
                            "Galpão 3",
                            "Atualizado");

            mockMvc.perform(
                            put("/doacoes/enderecos/{id}", endereco.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(endereco.getId()))
                    .andExpect(jsonPath("$.nome").value("Depósito Atualizado"))
                    .andExpect(jsonPath("$.numero").value("500"));
        }

        @Test
        @DisplayName("Deve retornar status 409 ao tentar atualizar para nome já existente")
        void atualizar_comNomeDuplicado_retornaStatus409() throws Exception {
            criarEnderecoExemplo("Depósito 1");
            EnderecoDoacao endereco2 = criarEnderecoExemplo("Depósito 2");

            var requisicao =
                    new EnderecoDoacaoDTO.Atualizacao(
                            "Depósito 1",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);

            mockMvc.perform(
                            put("/doacoes/enderecos/{id}", endereco2.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar endereço inexistente")
        void atualizar_comIdInexistente_retornaStatus404() throws Exception {
            var requisicao =
                    new EnderecoDoacaoDTO.Atualizacao(
                            "Depósito Teste",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);

            mockMvc.perform(
                            put("/doacoes/enderecos/{id}", 99999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/enderecos/{id}/desativar - Desativação (RN012)")
    class Desativar {

        @Test
        @DisplayName("Deve desativar endereço com sucesso retornando status 204")
        void desativar_semItensVinculados_retornaStatus204() throws Exception {
            EnderecoDoacao endereco = criarEnderecoExemplo("Depósito Para Desativar");

            mockMvc.perform(patch("/doacoes/enderecos/{id}/desativar", endereco.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/doacoes/enderecos/{id}", endereco.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar status 400 ao tentar desativar endereço com itens vinculados (RN012)")
        void desativar_comItensVinculados_retornaStatus400() throws Exception {
            EnderecoDoacao endereco = criarEnderecoExemplo("Depósito Com Itens");

            doReturn(true)
                    .when(enderecoDoacaoRepository)
                    .possuiItensVinculados(endereco.getId());

            mockMvc.perform(patch("/doacoes/enderecos/{id}/desativar", endereco.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar desativar endereço inexistente ou inativo")
        void desativar_comIdInexistenteOuJaInativo_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/doacoes/enderecos/{id}/desativar", 99999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/enderecos/{id}/reativar - Reativação")
    class Reativar {

        @Test
        @DisplayName("Deve reativar endereço com sucesso retornando status 204")
        void reativar_comIdExistente_retornaStatus204() throws Exception {
            EnderecoDoacao endereco = criarEnderecoExemplo("Depósito Para Reativar");
            endereco.desativar();
            endereco = enderecoDoacaoRepository.save(endereco);

            mockMvc.perform(patch("/doacoes/enderecos/{id}/reativar", endereco.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/doacoes/enderecos/{id}", endereco.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar reativar endereço inexistente")
        void reativar_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/doacoes/enderecos/{id}/reativar", 99999L))
                    .andExpect(status().isNotFound());
        }
    }
}
