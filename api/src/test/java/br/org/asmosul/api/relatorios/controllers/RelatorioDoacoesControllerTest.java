package br.org.asmosul.api.relatorios.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Entrada;
import br.org.asmosul.api.doacoes.models.EntradaEstoque;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.Saida;
import br.org.asmosul.api.doacoes.models.SaidaEstoque;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaRepository;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.models.Uf;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Relatórios de Doações - testes de integração dos sete endpoints")
class RelatorioDoacoesControllerTest extends BaseAPITest {

    @Autowired private CategoriaItemRepository categoriaRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private EnderecoArmazenamentoRepository enderecoRepository;
    @Autowired private EstoqueRepository estoqueRepository;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private EntradaRepository entradaRepository;
    @Autowired private EntradaEstoqueRepository entradaEstoqueRepository;
    @Autowired private SaidaRepository saidaRepository;
    @Autowired private SaidaEstoqueRepository saidaEstoqueRepository;

    private CategoriaItem categoria;
    private Item arroz;
    private EnderecoArmazenamento galpao;

    @BeforeEach
    void prepararDados() {
        // As alterações são desfeitas ao final de cada teste pelo BaseAPITest (@Transactional).
        categoria = categoriaRepository.save(new CategoriaItem("Alimentos", "Não perecíveis"));
        arroz = itemRepository.save(new Item("Arroz", categoria, 2500, UnidadeMedida.PACOTE, "Arroz 5kg"));
        Item feijao = itemRepository.save(new Item("Feijao", categoria, 1000, UnidadeMedida.PACOTE, "Feijão 1kg"));
        galpao = enderecoRepository.save(endereco("Galpao Central"));
        EnderecoArmazenamento filial = enderecoRepository.save(endereco("Filial Norte"));
        estoqueRepository.save(new Estoque(arroz, galpao, 10));
        estoqueRepository.save(new Estoque(arroz, filial, 5));
        estoqueRepository.save(new Estoque(feijao, galpao, 4));

        Pessoa pessoa = pessoaRepository.save(
                Pessoa.criarMinima("Doador Teste", "12345678901", br.org.asmosul.api.pessoas.models.TipoPessoa.FISICA, "63999999999"));
        Entrada entrada = entradaRepository.save(new Entrada(pessoa, LocalDateTime.now(), "Doação inicial"));
        entradaEstoqueRepository.save(new EntradaEstoque(arroz, galpao, entrada, 3));
        entradaEstoqueRepository.save(new EntradaEstoque(feijao, galpao, entrada, 2));

        Saida saida = saidaRepository.save(new Saida(pessoa, LocalDateTime.now(), "Distribuição"));
        saidaEstoqueRepository.save(new SaidaEstoque(arroz, saida, galpao, 2));
        saidaEstoqueRepository.save(new SaidaEstoque(feijao, saida, galpao, 1));
    }

    private EnderecoArmazenamento endereco(String nome) {
        return new EnderecoArmazenamento(
                nome, "77000-000", Uf.TO, "Palmas", "Centro", "Rua A", "10", null, null);
    }

    @Test
    @DisplayName("GET /itens/resumo: retorna os quatro Top 3")
    void resumoItens_retornaQuatroRankings() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/itens/resumo")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itensMaisSaem").isArray())
                .andExpect(jsonPath("$.itensMaisSaem[0].nome").value("Arroz"))
                .andExpect(jsonPath("$.itensMenosSaem").isArray())
                .andExpect(jsonPath("$.itensMaisEntram").isArray())
                .andExpect(jsonPath("$.itensMenosEntram").isArray());
    }

    @Test
    @DisplayName("GET /itens/tabela: aplica filtro, paginação e consolida saldos")
    void tabelaItens_filtraPeloNomeESaldo() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/itens/tabela")
                        .param("nomeItem", "Arroz")
                        .param("saldoEstoqueMin", "15")
                        .param("page", "0").param("size", "5")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.length()").value(1))
                .andExpect(jsonPath("$.dados[0].nomeItem").value("Arroz"))
                .andExpect(jsonPath("$.dados[0].unidadeMedida").value("PCT"))
                .andExpect(jsonPath("$.dados[0].saldoEstoque").value(15))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    @DisplayName("GET /itens/tabela: respeita a ordenação desc")
    void tabelaItens_respeitaOrdenacao() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/itens/tabela")
                        .param("sort", "nomeItem,desc")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].nomeItem").value("Feijao"));
    }

    @Test
    @DisplayName("GET /armazenamento: não duplica locais com vários itens")
    void resumoArmazenamento_contaLocaisCorretamente() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/armazenamento")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLocais").value(2))
                .andExpect(jsonPath("$.volumeFisicoTotal").value(19))
                .andExpect(jsonPath("$.locaisEmUso").value(2))
                .andExpect(jsonPath("$.distribuicaoVolumePorLocal.length()").value(2));
    }

    @Test
    @DisplayName("GET /estoque/totais: converte valores de centavos para reais")
    void totaisEstoque_retornaQuantidadeEReais() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/estoque/totais")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoFisicoGeral").value(19))
                .andExpect(jsonPath("$.valorTotalEstimado").value(415.0));
    }

    @Test
    @DisplayName("GET /estoque/tabela: filtra listas de IDs e valor mínimo em reais")
    void inventario_filtraCategoriasLocaisEValor() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/estoque/tabela")
                        .param("categoriasIds", categoria.getId().toString(), "999999")
                        .param("locaisIds", galpao.getId().toString(), "999999")
                        .param("valorMinimo", "100.00")
                        .param("sort", "valorTotalEstimado,desc")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.length()").value(1))
                .andExpect(jsonPath("$.dados[0].nomeItem").value("Arroz"))
                .andExpect(jsonPath("$.dados[0].quantidade").value(10))
                .andExpect(jsonPath("$.dados[0].valorUnitarioEstimado").value(25.0))
                .andExpect(jsonPath("$.dados[0].valorTotalEstimado").value(250.0));
    }

    @Test
    @DisplayName("GET /estoque/tabela: sem filtros não falha com listas ausentes")
    void inventario_semFiltros_retornaTodosOsRegistros() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/estoque/tabela")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.length()").value(3))
                .andExpect(jsonPath("$.totalElementos").value(3));
    }

    @Test
    @DisplayName("GET /categorias/resumo: retorna Top 5 e distribuição percentual")
    void resumoCategorias_contaItensDistintos() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/categorias/resumo")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItensCatalogo").value(2))
                .andExpect(jsonPath("$.top5MaisVariedade[0].nome").value("Alimentos"))
                .andExpect(jsonPath("$.distribuicaoParticipacao[0].percentual").value(100.0));
    }

    @Test
    @DisplayName("GET /categorias/tabela: agrupa variedade e saldo físico, com filtro")
    void tabelaCategorias_retornaSaldoEItensDaCategoria() throws Exception {
        mockMvc.perform(get("/relatorios/doacoes/categorias/tabela")
                        .param("termoBusca", "Alimen")
                        .with(user("gestor").roles("GERENCIADOR_RELATORIOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.length()").value(1))
                .andExpect(jsonPath("$.dados[0].nomeCategoria").value("Alimentos"))
                .andExpect(jsonPath("$.dados[0].itensDistintos").value(2))
                .andExpect(jsonPath("$.dados[0].saldoFisico").value(19));
    }
}
