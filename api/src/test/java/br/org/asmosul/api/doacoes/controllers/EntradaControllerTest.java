package br.org.asmosul.api.doacoes.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.doacoes.dtos.EntradaDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Entrada;
import br.org.asmosul.api.doacoes.models.EntradaEstoque;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.models.TipoPessoa;
import br.org.asmosul.api.pessoas.models.Uf;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("Testes de Integração - EntradaController")
class EntradaControllerTest extends BaseAPITest {

    @Autowired private EntradaRepository entradaRepository;
    @Autowired private EntradaEstoqueRepository entradaEstoqueRepository;
    @Autowired private EstoqueRepository estoqueRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CategoriaItemRepository categoriaItemRepository;
    @Autowired private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    @Autowired private PessoaRepository pessoaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Pessoa doador;
    private Item item;
    private EnderecoArmazenamento endereco;

    @BeforeEach
    void setUp() {
        entradaEstoqueRepository.deleteAll();
        entradaRepository.deleteAll();
        estoqueRepository.deleteAll();
        itemRepository.deleteAll();
        categoriaItemRepository.deleteAll();
        enderecoArmazenamentoRepository.deleteAll();
        pessoaRepository.deleteAll();

        doador =
                pessoaRepository.save(
                        Pessoa.criarMinima(
                                "Doador Teste",
                                "12345678901",
                                TipoPessoa.FISICA,
                                "63999999999"));

        CategoriaItem categoria =
                categoriaItemRepository.save(
                        new CategoriaItem("Alimentos Entrada", "Categoria para testes de entrada"));

        item =
                itemRepository.save(
                        new Item(
                                "Arroz 5kg",
                                categoria,
                                2500,
                                UnidadeMedida.PACOTE,
                                "Arroz para doação"));

        endereco =
                enderecoArmazenamentoRepository.save(
                        new EnderecoArmazenamento(
                                "Galpão Entrada",
                                "77000-000",
                                Uf.TO,
                                "Palmas",
                                "Centro",
                                "Avenida Principal",
                                "100",
                                null,
                                "Local de testes"));
    }

    @Nested
    @DisplayName("POST /doacoes/entradas")
    class Cadastrar {

        @Test
        @DisplayName("Deve criar entrada, incrementar estoque e marcar pessoa como doadora")
        void cadastrar_comDadosValidos_retorna201EAtualizaEstoque() throws Exception {
            var requisicao =
                    new EntradaDTO.Requisicao(
                            doador.getId(),
                            "Doação recebida",
                            List.of(
                                    new EntradaDTO.ItemEntradaRequisicao(
                                            item.getId(), endereco.getId(), 10)));

            mockMvc.perform(
                            post("/doacoes/entradas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            Matchers.matchesPattern(
                                                    ".*/doacoes/entradas/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.idDoador").value(doador.getId()))
                    .andExpect(jsonPath("$.descricao").value("Doação recebida"))
                    .andExpect(jsonPath("$.itens.length()").value(1))
                    .andExpect(jsonPath("$.itens[0].idItem").value(item.getId()))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(10));

            Estoque estoque =
                    estoqueRepository
                            .findByItemIdAndArmazenamentoId(item.getId(), endereco.getId())
                            .orElseThrow();
            org.assertj.core.api.Assertions.assertThat(estoque.getQuantidade()).isEqualTo(10);
            org.assertj.core.api.Assertions.assertThat(
                            pessoaRepository.findById(doador.getId()).orElseThrow().isEhDoador())
                    .isTrue();
        }

        @Test
        @DisplayName("Deve retornar 404 quando o doador não existir")
        void cadastrar_comDoadorInexistente_retorna404() throws Exception {
            var requisicao =
                    new EntradaDTO.Requisicao(
                            999999L,
                            "Entrada inválida",
                            List.of(
                                    new EntradaDTO.ItemEntradaRequisicao(
                                            item.getId(), endereco.getId(), 2)));

            mockMvc.perform(
                            post("/doacoes/entradas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 404 quando o item não existir")
        void cadastrar_comItemInexistente_retorna404() throws Exception {
            var requisicao =
                    new EntradaDTO.Requisicao(
                            doador.getId(),
                            "Entrada inválida",
                            List.of(
                                    new EntradaDTO.ItemEntradaRequisicao(
                                            999999L, endereco.getId(), 2)));

            mockMvc.perform(
                            post("/doacoes/entradas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 404 quando o endereço não existir")
        void cadastrar_comEnderecoInexistente_retorna404() throws Exception {
            var requisicao =
                    new EntradaDTO.Requisicao(
                            doador.getId(),
                            "Entrada inválida",
                            List.of(
                                    new EntradaDTO.ItemEntradaRequisicao(
                                            item.getId(), 999999L, 2)));

            mockMvc.perform(
                            post("/doacoes/entradas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /doacoes/entradas")
    class Listar {

        @Test
        @DisplayName("Deve listar entradas de forma paginada e permitir filtro por item")
        void listar_comEntradaCadastrada_retorna200EListaPaginada() throws Exception {
            Entrada entrada = criarEntradaComItem(4, 4);

            mockMvc.perform(
                            get("/doacoes/entradas")
                                    .param("itemId", item.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados.length()").value(1))
                    .andExpect(jsonPath("$.dados[0].id").value(entrada.getId()))
                    .andExpect(jsonPath("$.dados[0].itens[0].nomeItem").value("Arroz 5kg"))
                    .andExpect(jsonPath("$.dados[0].quantidadeTotalItens").value(4))
                    .andExpect(jsonPath("$.paginaAtual").value(0))
                    .andExpect(jsonPath("$.totalElementos").value(1));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/entradas/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar o detalhe completo da entrada")
        void buscarPorId_comIdExistente_retorna200() throws Exception {
            Entrada entrada = criarEntradaComItem(6, 6);

            mockMvc.perform(get("/doacoes/entradas/{id}", entrada.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(entrada.getId()))
                    .andExpect(jsonPath("$.nomeDoador").value("Doador Teste"))
                    .andExpect(jsonPath("$.itens.length()").value(1))
                    .andExpect(jsonPath("$.itens[0].idArmazenamento").value(endereco.getId()))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(6));
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/entradas/{id}")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar somente doador e descrição")
        void atualizar_comDadosValidos_retorna200() throws Exception {
            Entrada entrada = criarEntradaComItem(3, 3);
            Pessoa novoDoador =
                    pessoaRepository.save(
                            Pessoa.criarMinima(
                                    "Novo Doador",
                                    "98765432100",
                                    TipoPessoa.FISICA,
                                    "63988888888"));

            var atualizacao =
                    new EntradaDTO.Atualizacao(novoDoador.getId(), "Descrição atualizada");

            mockMvc.perform(
                            patch("/doacoes/entradas/{id}", entrada.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(atualizacao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idDoador").value(novoDoador.getId()))
                    .andExpect(jsonPath("$.nomeDoador").value("Novo Doador"))
                    .andExpect(jsonPath("$.descricao").value("Descrição atualizada"))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(3));
        }
    }

    @Nested
    @DisplayName("DELETE /doacoes/entradas/{id}")
    class DeletarEntrada {

        @Test
        @DisplayName("Deve excluir entrada e estornar o estoque")
        void deletar_comEntradaExistente_retorna204EEstornaEstoque() throws Exception {
            Entrada entrada = criarEntradaComItem(7, 20);

            mockMvc.perform(delete("/doacoes/entradas/{id}", entrada.getId()))
                    .andExpect(status().isNoContent());

            org.assertj.core.api.Assertions.assertThat(entradaRepository.existsById(entrada.getId()))
                    .isFalse();
            Estoque estoque =
                    estoqueRepository
                            .findByItemIdAndArmazenamentoId(item.getId(), endereco.getId())
                            .orElseThrow();
            org.assertj.core.api.Assertions.assertThat(estoque.getQuantidade()).isEqualTo(13);
        }
    }

    @Nested
    @DisplayName("DELETE /doacoes/entradas/{idEntrada}/estoque/{idEntradaEstoque}")
    class DeletarItemEntrada {

        @Test
        @DisplayName("Deve excluir registro individual e estornar sua quantidade")
        void deletarItemEntrada_comRegistroExistente_retorna204EEstornaEstoque()
                throws Exception {
            Entrada entrada =
                    entradaRepository.save(
                            new Entrada(doador, LocalDateTime.now(), "Entrada parcial"));
            EntradaEstoque entradaEstoque =
                    entradaEstoqueRepository.save(
                            new EntradaEstoque(item, endereco, entrada, 5));
            estoqueRepository.save(new Estoque(item, endereco, 12));

            mockMvc.perform(
                            delete(
                                    "/doacoes/entradas/{idEntrada}/estoque/{idEntradaEstoque}",
                                    entrada.getId(),
                                    entradaEstoque.getId()))
                    .andExpect(status().isNoContent());

            org.assertj.core.api.Assertions.assertThat(
                            entradaEstoqueRepository.existsById(entradaEstoque.getId()))
                    .isFalse();
            Estoque estoque =
                    estoqueRepository
                            .findByItemIdAndArmazenamentoId(item.getId(), endereco.getId())
                            .orElseThrow();
            org.assertj.core.api.Assertions.assertThat(estoque.getQuantidade()).isEqualTo(7);
        }
    }

    private Entrada criarEntradaComItem(int quantidadeEntrada, int quantidadeEstoque) {
        Entrada entrada =
                entradaRepository.save(
                        new Entrada(doador, LocalDateTime.now(), "Entrada de teste"));
        entradaEstoqueRepository.save(
                new EntradaEstoque(item, endereco, entrada, quantidadeEntrada));
        estoqueRepository.save(new Estoque(item, endereco, quantidadeEstoque));
        return entrada;
    }
}
