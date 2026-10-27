package br.org.asmosul.api.doacoes.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.doacoes.dtos.SaidaDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.Saida;
import br.org.asmosul.api.doacoes.models.SaidaEstoque;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaRepository;
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

@DisplayName("Testes de Integração - SaidaController")
class SaidaControllerTest extends BaseAPITest {

    @Autowired private SaidaRepository saidaRepository;
    @Autowired private SaidaEstoqueRepository saidaEstoqueRepository;
    @Autowired private EstoqueRepository estoqueRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CategoriaItemRepository categoriaItemRepository;
    @Autowired private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    @Autowired private PessoaRepository pessoaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Pessoa recebedor;
    private Item item;
    private EnderecoArmazenamento endereco;

    @BeforeEach
    void setUp() {
        saidaEstoqueRepository.deleteAll();
        saidaRepository.deleteAll();
        estoqueRepository.deleteAll();

        recebedor =
                pessoaRepository.save(
                        Pessoa.criarMinima(
                                "Recebedor Teste",
                                "12345678901",
                                TipoPessoa.FISICA,
                                "63999999999"));

        CategoriaItem categoria =
                categoriaItemRepository.save(
                        new CategoriaItem("Alimentos Saída", "Categoria para testes de saída"));

        item =
                itemRepository.save(
                        new Item(
                                "Feijão 1kg",
                                categoria,
                                1000,
                                UnidadeMedida.PACOTE,
                                "Feijão para doação"));

        endereco =
                enderecoArmazenamentoRepository.save(
                        new EnderecoArmazenamento(
                                "Galpão Saída",
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
    @DisplayName("POST /doacoes/saidas")
    class Cadastrar {

        @Test
        @DisplayName("Deve criar saída, abater estoque e marcar pessoa como beneficiária")
        void cadastrar_comSaldoSuficiente_retorna201EAbateEstoque() throws Exception {
            estoqueRepository.save(new Estoque(item, endereco, 10));
            var requisicao =
                    new SaidaDTO.Requisicao(
                            recebedor.getId(),
                            "Cesta entregue",
                            List.of(
                                    new SaidaDTO.ItemSaidaRequisicao(
                                            item.getId(), endereco.getId(), 4)));

            mockMvc.perform(
                            post("/doacoes/saidas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            Matchers.matchesPattern(".*/doacoes/saidas/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.idRecebedor").value(recebedor.getId()))
                    .andExpect(jsonPath("$.descricao").value("Cesta entregue"))
                    .andExpect(jsonPath("$.itens.length()").value(1))
                    .andExpect(jsonPath("$.itens[0].idItem").value(item.getId()))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(4));

            assertThat(quantidadeEmEstoque()).isEqualTo(6);
            assertThat(
                            pessoaRepository
                                    .findById(recebedor.getId())
                                    .orElseThrow()
                                    .isEhBeneficiario())
                    .isTrue();
        }

        @Test
        @DisplayName("Deve retornar 400 quando o saldo em estoque for insuficiente")
        void cadastrar_comSaldoInsuficiente_retorna400() throws Exception {
            estoqueRepository.save(new Estoque(item, endereco, 2));
            var requisicao =
                    new SaidaDTO.Requisicao(
                            recebedor.getId(),
                            null,
                            List.of(
                                    new SaidaDTO.ItemSaidaRequisicao(
                                            item.getId(), endereco.getId(), 3)));

            mockMvc.perform(
                            post("/doacoes/saidas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.invalidFields['itens[0].quantidade']")
                                    .value(Matchers.containsString("Saldo insuficiente")));
        }

        @Test
        @DisplayName("Deve retornar 404 quando o recebedor não existir")
        void cadastrar_comRecebedorInexistente_retorna404() throws Exception {
            var requisicao =
                    new SaidaDTO.Requisicao(
                            999999L,
                            null,
                            List.of(
                                    new SaidaDTO.ItemSaidaRequisicao(
                                            item.getId(), endereco.getId(), 1)));

            mockMvc.perform(
                            post("/doacoes/saidas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 404 quando o item não existir")
        void cadastrar_comItemInexistente_retorna404() throws Exception {
            var requisicao =
                    new SaidaDTO.Requisicao(
                            recebedor.getId(),
                            null,
                            List.of(
                                    new SaidaDTO.ItemSaidaRequisicao(
                                            999999L, endereco.getId(), 1)));

            mockMvc.perform(
                            post("/doacoes/saidas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 404 quando o endereço não existir")
        void cadastrar_comEnderecoInexistente_retorna404() throws Exception {
            var requisicao =
                    new SaidaDTO.Requisicao(
                            recebedor.getId(),
                            null,
                            List.of(new SaidaDTO.ItemSaidaRequisicao(item.getId(), 999999L, 1)));

            mockMvc.perform(
                            post("/doacoes/saidas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /doacoes/saidas")
    class Listar {

        @Test
        @DisplayName("Deve listar saídas de forma paginada e permitir filtro por item")
        void listar_comSaidaCadastrada_retorna200EListaPaginada() throws Exception {
            Saida saida = criarSaidaComItens(5);

            mockMvc.perform(get("/doacoes/saidas").param("itensIds", item.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados.length()").value(1))
                    .andExpect(jsonPath("$.dados[0].id").value(saida.getId()))
                    .andExpect(jsonPath("$.dados[0].nomeRecebedor").value("Recebedor Teste"))
                    .andExpect(jsonPath("$.dados[0].itensOuKitsDescricao[0]").value("Feijão 1kg"))
                    .andExpect(jsonPath("$.dados[0].quantidadeTotal").value(5))
                    .andExpect(jsonPath("$.paginaAtual").value(0))
                    .andExpect(jsonPath("$.totalElementos").value(1));
        }
    }

    @Nested
    @DisplayName("GET /doacoes/saidas/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar o detalhe completo da saída")
        void buscarPorId_comIdExistente_retorna200() throws Exception {
            Saida saida = criarSaidaComItens(6);

            mockMvc.perform(get("/doacoes/saidas/{id}", saida.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saida.getId()))
                    .andExpect(jsonPath("$.nomeRecebedor").value("Recebedor Teste"))
                    .andExpect(jsonPath("$.itens.length()").value(1))
                    .andExpect(jsonPath("$.itens[0].idArmazenamento").value(endereco.getId()))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(6));
        }
    }

    @Nested
    @DisplayName("PATCH /doacoes/saidas/{id}")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar somente recebedor e descrição")
        void atualizar_comDadosValidos_retorna200() throws Exception {
            Saida saida = criarSaidaComItens(3);
            Pessoa novoRecebedor =
                    pessoaRepository.save(
                            Pessoa.criarMinima(
                                    "Novo Recebedor",
                                    "98765432100",
                                    TipoPessoa.FISICA,
                                    "63988888888"));

            var atualizacao =
                    new SaidaDTO.Atualizacao(novoRecebedor.getId(), "Descrição atualizada");

            mockMvc.perform(
                            patch("/doacoes/saidas/{id}", saida.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(atualizacao)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idRecebedor").value(novoRecebedor.getId()))
                    .andExpect(jsonPath("$.nomeRecebedor").value("Novo Recebedor"))
                    .andExpect(jsonPath("$.descricao").value("Descrição atualizada"))
                    .andExpect(jsonPath("$.itens[0].quantidade").value(3));

            assertThat(novoRecebedor.isEhBeneficiario()).isTrue();
        }
    }

    @Nested
    @DisplayName("DELETE /doacoes/saidas/{id}")
    class DeletarSaida {

        @Test
        @DisplayName("Deve excluir saída com seus itens e estornar o estoque")
        void deletar_comSaidaExistente_retorna204EEstornaEstoque() throws Exception {
            estoqueRepository.save(new Estoque(item, endereco, 10));
            Saida saida = criarSaidaComItens(4, 3);

            mockMvc.perform(delete("/doacoes/saidas/{id}", saida.getId()))
                    .andExpect(status().isNoContent());

            assertThat(saidaRepository.existsById(saida.getId())).isFalse();
            assertThat(saidaEstoqueRepository.countBySaidaId(saida.getId())).isZero();
            assertThat(quantidadeEmEstoque()).isEqualTo(17);
        }
    }

    @Nested
    @DisplayName("DELETE /doacoes/saidas/{idSaida}/estoque/{idSaidaEstoque}")
    class DeletarItemSaida {

        @Test
        @DisplayName("Deve excluir registro individual e estornar sua quantidade")
        void deletarItemSaida_comOutrosItens_retorna204EEstornaEstoque() throws Exception {
            estoqueRepository.save(new Estoque(item, endereco, 10));
            Saida saida = criarSaidaComItens(4, 3);
            SaidaEstoque primeiro =
                    saidaEstoqueRepository.findAllBySaidaIdComRelacionamentos(saida.getId()).get(0);

            mockMvc.perform(
                            delete(
                                    "/doacoes/saidas/{idSaida}/estoque/{idSaidaEstoque}",
                                    saida.getId(),
                                    primeiro.getId()))
                    .andExpect(status().isNoContent());

            assertThat(saidaEstoqueRepository.existsById(primeiro.getId())).isFalse();
            assertThat(saidaEstoqueRepository.countBySaidaId(saida.getId())).isEqualTo(1);
            assertThat(quantidadeEmEstoque()).isEqualTo(14);
        }

        @Test
        @DisplayName("Deve retornar 400 ao tentar excluir o último item da saída")
        void deletarItemSaida_ultimoItem_retorna400() throws Exception {
            estoqueRepository.save(new Estoque(item, endereco, 10));
            Saida saida = criarSaidaComItens(4);
            SaidaEstoque unico =
                    saidaEstoqueRepository.findAllBySaidaIdComRelacionamentos(saida.getId()).get(0);

            mockMvc.perform(
                            delete(
                                    "/doacoes/saidas/{idSaida}/estoque/{idSaidaEstoque}",
                                    saida.getId(),
                                    unico.getId()))
                    .andExpect(status().isBadRequest());

            assertThat(saidaEstoqueRepository.existsById(unico.getId())).isTrue();
            assertThat(quantidadeEmEstoque()).isEqualTo(10);
        }

        @Test
        @DisplayName("Deve retornar 404 quando o registro pertencer a outra saída")
        void deletarItemSaida_registroDeOutraSaida_retorna404() throws Exception {
            Saida saida = criarSaidaComItens(1, 1);
            Saida outraSaida = criarSaidaComItens(2);
            SaidaEstoque registroDaOutra =
                    saidaEstoqueRepository
                            .findAllBySaidaIdComRelacionamentos(outraSaida.getId())
                            .get(0);

            mockMvc.perform(
                            delete(
                                    "/doacoes/saidas/{idSaida}/estoque/{idSaidaEstoque}",
                                    saida.getId(),
                                    registroDaOutra.getId()))
                    .andExpect(status().isNotFound());

            assertThat(saidaEstoqueRepository.existsById(registroDaOutra.getId())).isTrue();
        }
    }

    private Saida criarSaidaComItens(int... quantidades) {
        Saida saida =
                saidaRepository.save(new Saida(recebedor, LocalDateTime.now(), "Saída de teste"));
        for (int quantidade : quantidades) {
            saidaEstoqueRepository.save(new SaidaEstoque(item, saida, endereco, quantidade));
        }
        return saida;
    }

    private int quantidadeEmEstoque() {
        return estoqueRepository
                .findByItemIdAndArmazenamentoId(item.getId(), endereco.getId())
                .orElseThrow()
                .getQuantidade();
    }
}
