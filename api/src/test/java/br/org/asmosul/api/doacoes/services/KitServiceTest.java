package br.org.asmosul.api.doacoes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.doacoes.dtos.KitDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.ItemKit;
import br.org.asmosul.api.doacoes.models.Kit;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemKitRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.KitRepository;
import br.org.asmosul.api.pessoas.models.Uf;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - KitService")
class KitServiceTest {

    @Mock private KitRepository kitRepository;
    @Mock private ItemKitRepository itemKitRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private EstoqueRepository estoqueRepository;
    @Mock private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    @InjectMocks private KitService kitService;

    private Item itemArroz;
    private Item itemFeijao;
    private EnderecoArmazenamento enderecoGalpao;

    @BeforeEach
    void setUp() {
        CategoriaItem categoria = new CategoriaItem("Alimentos", "Categoria Alimentos");
        categoria.setId(1L);

        itemArroz = new Item("Arroz 5kg", categoria, 2500, UnidadeMedida.PACOTE, "Arroz");
        itemArroz.setId(10L);

        itemFeijao = new Item("Feijão 1kg", categoria, 800, UnidadeMedida.PACOTE, "Feijão");
        itemFeijao.setId(20L);

        enderecoGalpao =
                new EnderecoArmazenamento(
                        "Galpão Central",
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Centro",
                        "Av. Principal",
                        "100",
                        null,
                        null);
        enderecoGalpao.setId(100L);
    }

    @Test
    @DisplayName(
            "Cadastro deve normalizar nome, salvar kit e itens e calcular quantidade distribuível")
    void cadastrar_comDadosValidos_normalizaNomeESalvaKitEItens() {
        when(kitRepository.existsByNomeIgnoreCase("Cesta Básica Familiar")).thenReturn(false);
        when(itemRepository.findByIdAndDataInativoIsNull(10L)).thenReturn(Optional.of(itemArroz));
        when(itemRepository.findByIdAndDataInativoIsNull(20L)).thenReturn(Optional.of(itemFeijao));
        when(kitRepository.save(any(Kit.class)))
                .thenAnswer(
                        invocation -> {
                            Kit k = invocation.getArgument(0);
                            k.setId(1L);
                            return k;
                        });
        when(itemKitRepository.save(any(ItemKit.class)))
                .thenAnswer(
                        invocation -> {
                            ItemKit ik = invocation.getArgument(0);
                            ik.setId(1000L);
                            return ik;
                        });
        when(estoqueRepository.somarQuantidadePorItemIds(anyCollection()))
                .thenReturn(
                        List.<Object[]>of(
                                new Object[] {10L, 10L}, // 10 / 2 = 5 kits
                                new Object[] {20L, 9L} // 9 / 3 = 3 kits -> min = 3
                                ));

        var requisicao =
                new KitDTO.Requisicao(
                        "  Cesta   Básica   Familiar  ",
                        "Descrição da cesta",
                        List.of(
                                new KitDTO.ItemKitRequisicao(10L, 2),
                                new KitDTO.ItemKitRequisicao(20L, 3)));

        KitDTO.Detalhe detalhe = kitService.cadastrar(requisicao);

        assertThat(detalhe.id()).isEqualTo(1L);
        assertThat(detalhe.nome()).isEqualTo("Cesta Básica Familiar");
        assertThat(detalhe.descricao()).isEqualTo("Descrição da cesta");
        assertThat(detalhe.quantidadeDistribuivel()).isEqualTo(3);
        assertThat(detalhe.totalItens()).isEqualTo(2);
        assertThat(detalhe.ativo()).isTrue();
    }

    @Test
    @DisplayName("Cadastro deve lançar ConflitoDadosException quando nome já existir (409)")
    void cadastrar_comNomeDuplicado_lancaConflitoDadosException() {
        when(kitRepository.existsByNomeIgnoreCase("cesta básica")).thenReturn(true);

        var requisicao =
                new KitDTO.Requisicao(
                        "  cesta   básica ",
                        "Descrição",
                        List.of(new KitDTO.ItemKitRequisicao(10L, 1)));

        assertThatThrownBy(() -> kitService.cadastrar(requisicao))
                .isInstanceOf(ConflitoDadosException.class)
                .hasMessageContaining("Já existe um kit cadastrado com este nome");
    }

    @Test
    @DisplayName(
            "Cadastro deve lançar ValidationException quando houver itens repetidos no mesmo kit (400)")
    void cadastrar_comItensDuplicadosNoMesmoKit_lancaValidationException() {
        when(kitRepository.existsByNomeIgnoreCase("Kit Higiene")).thenReturn(false);

        var requisicao =
                new KitDTO.Requisicao(
                        "Kit Higiene",
                        "Descrição",
                        List.of(
                                new KitDTO.ItemKitRequisicao(10L, 1),
                                new KitDTO.ItemKitRequisicao(10L, 2)));

        assertThatThrownBy(() -> kitService.cadastrar(requisicao))
                .isInstanceOf(ValidationException.class)
                .satisfies(
                        ex -> {
                            ValidationException vex = (ValidationException) ex;
                            assertThat(vex.getErros()).isNotEmpty();
                            assertThat(vex.getErros().get(0).mensagem())
                                    .contains(
                                            "Não é permitido adicionar o mesmo item mais de uma vez");
                        });
    }

    @Test
    @DisplayName(
            "Disponibilidade por local deve calcular o máximo de kits possíveis como min(floor(saldo / necessario))")
    void consultarDisponibilidade_calculaMaxKitsPorLocalCorretamente() {
        Kit kit = new Kit("Kit Alimentação", "Kit padrão");
        kit.setId(5L);

        ItemKit ikArroz = new ItemKit(itemArroz, kit, 2);
        ikArroz.setId(51L);
        ItemKit ikFeijao = new ItemKit(itemFeijao, kit, 4);
        ikFeijao.setId(52L);

        Estoque estoqueArroz = new Estoque(itemArroz, enderecoGalpao, 15); // floor(15 / 2) = 7
        Estoque estoqueFeijao = new Estoque(itemFeijao, enderecoGalpao, 11); // floor(11 / 4) = 2

        when(kitRepository.findByIdAndDataInativoIsNull(5L)).thenReturn(Optional.of(kit));
        when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(100L))
                .thenReturn(Optional.of(enderecoGalpao));
        when(itemKitRepository.findAllByKitIdComItem(5L)).thenReturn(List.of(ikArroz, ikFeijao));
        when(estoqueRepository.findAllByArmazenamentoIdAndItemIdIn(100L, List.of(10L, 20L)))
                .thenReturn(List.of(estoqueArroz, estoqueFeijao));

        KitDTO.Disponibilidade disponibilidade = kitService.consultarDisponibilidade(5L, 100L);

        assertThat(disponibilidade.idKit()).isEqualTo(5L);
        assertThat(disponibilidade.localArmazenamentoId()).isEqualTo(100L);
        assertThat(disponibilidade.quantidadeDisponivel()).isEqualTo(2);
        assertThat(disponibilidade.itens()).hasSize(2);
    }

    @Test
    @DisplayName(
            "Disponibilidade por local deve retornar zero quando algum item não tiver saldo suficiente")
    void consultarDisponibilidade_quandoItemSemSaldoNoLocal_retornaZero() {
        Kit kit = new Kit("Kit Alimentação", "Kit padrão");
        kit.setId(5L);

        ItemKit ikArroz = new ItemKit(itemArroz, kit, 2);
        ikArroz.setId(51L);
        ItemKit ikFeijao = new ItemKit(itemFeijao, kit, 4);
        ikFeijao.setId(52L);

        Estoque estoqueArroz = new Estoque(itemArroz, enderecoGalpao, 20); // floor(20 / 2) = 10
        // Feijão não tem registro no local (saldo 0 -> floor(0 / 4) = 0)

        when(kitRepository.findByIdAndDataInativoIsNull(5L)).thenReturn(Optional.of(kit));
        when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(100L))
                .thenReturn(Optional.of(enderecoGalpao));
        when(itemKitRepository.findAllByKitIdComItem(5L)).thenReturn(List.of(ikArroz, ikFeijao));
        when(estoqueRepository.findAllByArmazenamentoIdAndItemIdIn(100L, List.of(10L, 20L)))
                .thenReturn(List.of(estoqueArroz));

        KitDTO.Disponibilidade disponibilidade = kitService.consultarDisponibilidade(5L, 100L);

        assertThat(disponibilidade.quantidadeDisponivel()).isZero();
    }

    @Test
    @DisplayName("Desativar e reativar devem alternar estado lógico do kit")
    void desativarEReativar_alternamEstadoLogicoDoKit() {
        Kit kit = new Kit("Kit Inverno", "Cobertores e agasalhos");
        kit.setId(7L);

        when(kitRepository.findByIdAndDataInativoIsNull(7L)).thenReturn(Optional.of(kit));
        kitService.desativar(7L);

        assertThat(kit.isAtivo()).isFalse();
        assertThat(kit.getDataInativo()).isNotNull();

        when(kitRepository.findById(7L)).thenReturn(Optional.of(kit));
        kitService.reativar(7L);

        assertThat(kit.isAtivo()).isTrue();
        assertThat(kit.getDataInativo()).isNull();
    }

    @Test
    @DisplayName("BuscarPorId com ID inexistente deve lançar EntidadeNaoEncontradaException")
    void buscarPorId_inexistente_lancaEntidadeNaoEncontradaException() {
        when(kitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kitService.buscarPorId(999L))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }
}
