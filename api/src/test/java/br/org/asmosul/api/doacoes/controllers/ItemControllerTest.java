package br.org.asmosul.api.doacoes.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.doacoes.dtos.ItemDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.pessoas.models.Uf;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Testes de Integração - ItemController")
class ItemControllerTest extends BaseAPITest {

    @Autowired private ItemRepository itemRepository;
    @Autowired private CategoriaItemRepository categoriaItemRepository;
    @Autowired private EstoqueRepository estoqueRepository;
    @Autowired private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private CategoriaItem categoriaPadrao;

    @BeforeEach
    void setUp() {
        estoqueRepository.deleteAll();
        itemRepository.deleteAll();
        categoriaItemRepository.deleteAll();
        enderecoArmazenamentoRepository.deleteAll();

        categoriaPadrao =
                categoriaItemRepository.save(
                        new CategoriaItem(
                                "Alimentos Não Perecíveis", "Arroz, feijão, macarrão, etc."));
    }

    @Nested
    @DisplayName("POST /doacoes/itens - Cadastro de Item")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar item com sucesso retornando status 201 e Location header")
        void cadastrar_comDadosValidos_retornaStatus201ELocationHeader() throws Exception {
            var requisicao =
                    new ItemDTO.Requisicao(
                            "Arroz Branco 5kg",
                            categoriaPadrao.getId(),
                            2500,
                            UnidadeMedida.PACOTE,
                            "Pacote de arroz tipo 1");

            mockMvc.perform(
                            post("/doacoes/itens")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            Matchers.matchesPattern(".*/doacoes/itens/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("Arroz Branco 5kg"))
                    .andExpect(jsonPath("$.categoria.id").value(categoriaPadrao.getId()))
                    .andExpect(jsonPath("$.categoria.nome").value(categoriaPadrao.getNome()))
                    .andExpect(jsonPath("$.precoUnitario").value(2500))
                    .andExpect(jsonPath("$.unidadeMedida.codigo").value("PCT"))
                    .andExpect(jsonPath("$.estoque").value(0))
                    .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @DisplayName("Deve retornar status 409 quando o nome já estiver cadastrado (RN23)")
        void cadastrar_comNomeDuplicado_retornaStatus409() throws Exception {
            itemRepository.save(
                    new Item(
                            "Feijão Carioca 1kg",
                            categoriaPadrao,
                            800,
                            UnidadeMedida.PACOTE,
                            "Feijão"));

            var requisicao =
                    new ItemDTO.Requisicao(
                            "feijão carioca 1kg",
                            categoriaPadrao.getId(),
                            850,
                            UnidadeMedida.PACOTE,
                            "Outro feijão");

            mockMvc.perform(
                            post("/doacoes/itens")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome estiver em branco")
        void cadastrar_comNomeEmBranco_retornaStatus400() throws Exception {
            var requisicao =
                    new ItemDTO.Requisicao(
                            "", categoriaPadrao.getId(), 1000, UnidadeMedida.UNIDADE, "Sem nome");

            mockMvc.perform(
                            post("/doacoes/itens")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando a categoria não existir ou for inválida")
        void cadastrar_comCategoriaInexistente_retornaStatus400() throws Exception {
            var requisicao =
                    new ItemDTO.Requisicao(
                            "Macarrão Espaguete", 999999L, 450, UnidadeMedida.PACOTE, "Macarrão");

            mockMvc.perform(
                            post("/doacoes/itens")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o preço unitário for negativo")
        void cadastrar_comPrecoUnitarioNegativo_retornaStatus400() throws Exception {
            var requisicao =
                    new ItemDTO.Requisicao(
                            "Óleo de Soja 900ml",
                            categoriaPadrao.getId(),
                            -10,
                            UnidadeMedida.UNIDADE,
                            "Óleo");

            mockMvc.perform(
                            post("/doacoes/itens")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /doacoes/itens - Listagem Paginada de Itens")
    class Listar {

        @Test
        @DisplayName("Deve retornar listagem paginada com status 200")
        void listar_comSucesso_retornaStatus200EListaPaginada() throws Exception {
            itemRepository.save(
                    new Item(
                            "Açúcar Refinado 1kg",
                            categoriaPadrao,
                            400,
                            UnidadeMedida.PACOTE,
                            "Açúcar"));
            itemRepository.save(
                    new Item(
                            "Sal Refinado 1kg", categoriaPadrao, 200, UnidadeMedida.PACOTE, "Sal"));

            mockMvc.perform(get("/doacoes/itens"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados").isArray())
                    .andExpect(jsonPath("$.dados.length()").value(2))
                    .andExpect(jsonPath("$.paginaAtual").value(0))
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }

        @Test
        @DisplayName("Deve filtrar itens por nome")
        void listar_comFiltroNome_retornaApenasItensFiltrados() throws Exception {
            itemRepository.save(
                    new Item(
                            "Leite Integral 1L",
                            categoriaPadrao,
                            500,
                            UnidadeMedida.UNIDADE,
                            "Leite"));
            itemRepository.save(
                    new Item(
                            "Café Torrado 500g",
                            categoriaPadrao,
                            1500,
                            UnidadeMedida.PACOTE,
                            "Café"));

            mockMvc.perform(get("/doacoes/itens").param("nome", "leite"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados.length()").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Leite Integral 1L"));
        }

        @Test
        @DisplayName("Deve filtrar apenas itens desativados quando apenasInativos=true (US-66)")
        void listar_comApenasInativos_retornaApenasItensInativos() throws Exception {
            Item ativo =
                    itemRepository.save(
                            new Item(
                                    "Item Ativo",
                                    categoriaPadrao,
                                    100,
                                    UnidadeMedida.UNIDADE,
                                    "Ativo"));
            Item inativo =
                    itemRepository.save(
                            new Item(
                                    "Item Inativo",
                                    categoriaPadrao,
                                    100,
                                    UnidadeMedida.UNIDADE,
                                    "Inativo"));
            inativo.desativar();
            itemRepository.save(inativo);

            mockMvc.perform(get("/doacoes/itens").param("apenasInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados.length()").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Item Inativo"))
                    .andExpect(jsonPath("$.dados[0].ativo").value(false));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/itens/todos - Listagem Completa para Seleção")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar todos os itens ativos sem paginação com status 200")
        void listarTodos_retornaStatus200EListaSimples() throws Exception {
            itemRepository.save(
                    new Item("Item 1", categoriaPadrao, 100, UnidadeMedida.UNIDADE, "1"));
            itemRepository.save(
                    new Item("Item 2", categoriaPadrao, 200, UnidadeMedida.UNIDADE, "2"));

            Item inativo =
                    itemRepository.save(
                            new Item("Item 3", categoriaPadrao, 300, UnidadeMedida.UNIDADE, "3"));
            inativo.desativar();
            itemRepository.save(inativo);

            mockMvc.perform(get("/doacoes/itens/todos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/itens/{id} - Buscar Item por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes completos do item incluindo locais e estoque (US-63)")
        void buscarPorId_comIdExistente_retornaStatus200EDetalhesComLocais() throws Exception {
            Item item =
                    itemRepository.save(
                            new Item(
                                    "Farinha de Trigo 1kg",
                                    categoriaPadrao,
                                    450,
                                    UnidadeMedida.PACOTE,
                                    "Farinha"));

            EnderecoArmazenamento endereco =
                    enderecoArmazenamentoRepository.save(
                            new EnderecoArmazenamento(
                                    "Galpão Central",
                                    "77000-000",
                                    Uf.TO,
                                    "Palmas",
                                    "Plano Diretor Sul",
                                    "Av. Teotônio Segurado",
                                    "S/N",
                                    null,
                                    "Setor de armazenamento principal"));

            estoqueRepository.save(new Estoque(item, endereco, 150));

            mockMvc.perform(get("/doacoes/itens/{id}", item.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(item.getId()))
                    .andExpect(jsonPath("$.nome").value("Farinha de Trigo 1kg"))
                    .andExpect(jsonPath("$.estoque").value(150))
                    .andExpect(jsonPath("$.locais").isArray())
                    .andExpect(jsonPath("$.locais.length()").value(1))
                    .andExpect(jsonPath("$.locais[0].idEndereco").value(endereco.getId()))
                    .andExpect(jsonPath("$.locais[0].nomeEndereco").value("Galpão Central"))
                    .andExpect(jsonPath("$.locais[0].quantidade").value(150));
        }

        @Test
        @DisplayName("Deve retornar status 404 quando o item não for encontrado")
        void buscarPorId_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(get("/doacoes/itens/{id}", 999999L)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /doacoes/itens/{id} - Atualizar Item")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar item com sucesso retornando status 200")
        void atualizar_comDadosValidos_retornaStatus200() throws Exception {
            Item item =
                    itemRepository.save(
                            new Item(
                                    "Extrato de Tomate",
                                    categoriaPadrao,
                                    300,
                                    UnidadeMedida.LATA,
                                    "Extrato"));

            var requisicao =
                    new ItemDTO.Atualizacao(
                            "Molho de Tomate 340g",
                            categoriaPadrao.getId(),
                            350,
                            UnidadeMedida.PACOTE,
                            "Sachê de molho pronto");

            mockMvc.perform(
                            put("/doacoes/itens/{id}", item.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(item.getId()))
                    .andExpect(jsonPath("$.nome").value("Molho de Tomate 340g"))
                    .andExpect(jsonPath("$.precoUnitario").value(350))
                    .andExpect(jsonPath("$.unidadeMedida.codigo").value("PCT"));
        }

        @Test
        @DisplayName(
                "Deve retornar status 409 quando atualizar para nome já existente em outro item")
        void atualizar_comNomeDuplicado_retornaStatus409() throws Exception {
            itemRepository.save(
                    new Item("Item Existente", categoriaPadrao, 200, UnidadeMedida.UNIDADE, ""));
            Item itemParaAtualizar =
                    itemRepository.save(
                            new Item(
                                    "Item Secundário",
                                    categoriaPadrao,
                                    200,
                                    UnidadeMedida.UNIDADE,
                                    ""));

            var requisicao =
                    new ItemDTO.Atualizacao(
                            "item existente",
                            categoriaPadrao.getId(),
                            250,
                            UnidadeMedida.UNIDADE,
                            "");

            mockMvc.perform(
                            put("/doacoes/itens/{id}", itemParaAtualizar.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }

        @Test
        @DisplayName(
                "Deve retornar status 404 quando o item para atualizar não existir ou estiver inativo")
        void atualizar_comIdInexistente_retornaStatus404() throws Exception {
            var requisicao =
                    new ItemDTO.Atualizacao(
                            "Item Fantasma",
                            categoriaPadrao.getId(),
                            500,
                            UnidadeMedida.UNIDADE,
                            "");

            mockMvc.perform(
                            put("/doacoes/itens/{id}", 999999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/itens/{id}/desativar - Desativação Lógica")
    class Desativar {

        @Test
        @DisplayName("Deve desativar item com sucesso retornando status 204")
        void desativar_comIdExistente_retornaStatus204() throws Exception {
            Item item =
                    itemRepository.save(
                            new Item(
                                    "Item Para Desativar",
                                    categoriaPadrao,
                                    100,
                                    UnidadeMedida.UNIDADE,
                                    ""));

            mockMvc.perform(patch("/doacoes/itens/{id}/desativar", item.getId()))
                    .andExpect(status().isNoContent());

            Item itemAtualizado = itemRepository.findById(item.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertFalse(itemAtualizado.isAtivo());
            org.junit.jupiter.api.Assertions.assertNotNull(itemAtualizado.getDataInativo());
        }

        @Test
        @DisplayName("Deve retornar status 404 quando desativar item inexistente ou já inativo")
        void desativar_comItemJaInativo_retornaStatus404() throws Exception {
            Item item =
                    itemRepository.save(
                            new Item(
                                    "Item Já Inativo",
                                    categoriaPadrao,
                                    100,
                                    UnidadeMedida.UNIDADE,
                                    ""));
            item.desativar();
            itemRepository.save(item);

            mockMvc.perform(patch("/doacoes/itens/{id}/desativar", item.getId()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/itens/{id}/reativar - Reativação de Item")
    class Reativar {

        @Test
        @DisplayName("Deve reativar item previamente desativado retornando status 204")
        void reativar_comItemInativo_retornaStatus204() throws Exception {
            Item item =
                    itemRepository.save(
                            new Item(
                                    "Item Desativado",
                                    categoriaPadrao,
                                    100,
                                    UnidadeMedida.UNIDADE,
                                    ""));
            item.desativar();
            itemRepository.save(item);

            mockMvc.perform(patch("/doacoes/itens/{id}/reativar", item.getId()))
                    .andExpect(status().isNoContent());

            Item itemAtualizado = itemRepository.findById(item.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertTrue(itemAtualizado.isAtivo());
            org.junit.jupiter.api.Assertions.assertNull(itemAtualizado.getDataInativo());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar reativar item inexistente")
        void reativar_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/doacoes/itens/{id}/reativar", 999999L))
                    .andExpect(status().isNotFound());
        }
    }
}
