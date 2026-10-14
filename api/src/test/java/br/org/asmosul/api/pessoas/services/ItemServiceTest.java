package br.org.asmosul.api.pessoas.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.pessoas.dtos.ItemDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.models.Item;
import br.org.asmosul.api.pessoas.models.UnidadeMedida;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import br.org.asmosul.api.pessoas.repositories.ItemRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ItemService")
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private CategoriaRepository categoriaRepository;

    @InjectMocks private ItemService itemService;

    private Categoria categoriaPadrao;

    @BeforeEach
    void setUp() {
        categoriaPadrao = new Categoria("Alimentos Não Perecíveis", "Cestas básicas e suprimentos");
        categoriaPadrao.setId(1L);
    }

    private ItemDTO.Requisicao criarRequisicaoValida() {
        return new ItemDTO.Requisicao(
                "Arroz 5kg",
                1L,
                15.0,
                UnidadeMedida.KG,
                "Arroz tipo 1 agulhinha");
    }

    private Item criarItemTeste() {
        Item item =
                new Item(
                        "Arroz 5kg",
                        categoriaPadrao,
                        15.0,
                        UnidadeMedida.KG,
                        "Arroz tipo 1 agulhinha");
        item.setId(1L);
        return item;
    }

    @Nested
    @DisplayName("Cadastrar Item - Cenários e Regras")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar item com sucesso")
        void cadastrarItem_comDadosValidos_salvaERetornaDetalhe() {
            var req = criarRequisicaoValida();

            when(itemRepository.existsByNomeIgnoreCase("Arroz 5kg")).thenReturn(false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaPadrao));
            when(itemRepository.save(any(Item.class)))
                    .thenAnswer(
                            inv -> {
                                Item i = inv.getArgument(0);
                                i.setId(10L);
                                return i;
                            });
            when(itemRepository.calcularEstoqueAtual(10L)).thenReturn(0.0);

            ItemDTO.Detalhe detalhe = itemService.cadastrar(req);

            assertThat(detalhe).isNotNull();
            assertThat(detalhe.id()).isEqualTo(10L);
            assertThat(detalhe.nome()).isEqualTo("Arroz 5kg");
            assertThat(detalhe.unidadeMedida()).isEqualTo(UnidadeMedida.KG);
            assertThat(detalhe.categoria().id()).isEqualTo(1L);
            assertThat(detalhe.ativo()).isTrue();

            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException se já existir item com mesmo nome")
        void cadastrarItem_comNomeExistente_lancaConflitoDadosException() {
            var req = criarRequisicaoValida();

            when(itemRepository.existsByNomeIgnoreCase("Arroz 5kg")).thenReturn(true);

            assertThatThrownBy(() -> itemService.cadastrar(req))
                    .isInstanceOf(ConflitoDadosException.class)
                    .hasMessageContaining("Já existe um item cadastrado com o nome");

            verify(categoriaRepository, never()).findById(any());
            verify(itemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException se categoria não existir")
        void cadastrarItem_comCategoriaInexistente_lancaEntidadeNaoEncontradaException() {
            var req = criarRequisicaoValida();

            when(itemRepository.existsByNomeIgnoreCase("Arroz 5kg")).thenReturn(false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.cadastrar(req))
                    .isInstanceOf(EntidadeNaoEncontradaException.class)
                    .hasMessageContaining("Categoria não encontrada");

            verify(itemRepository, never()).save(any(Item.class));
        }
    }

    @Nested
    @DisplayName("Atualizar Item - Cenários e Regras")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar item com sucesso")
        void atualizarItem_comDadosValidos_atualizaERetornaDetalhe() {
            Item itemExistente = criarItemTeste();

            when(itemRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.of(itemExistente));
            when(itemRepository.existsByNomeIgnoreCaseAndIdNot("Arroz 5kg Tio João", 1L)).thenReturn(false);
            when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaPadrao));
            when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
            when(itemRepository.calcularEstoqueAtual(1L)).thenReturn(12.5);

            var atualizacao =
                    new ItemDTO.Atualizacao(
                            "Arroz 5kg Tio João",
                            1L,
                            18.5,
                            UnidadeMedida.KG,
                            "Arroz parboilizado tipo 1");

            ItemDTO.Detalhe detalhe = itemService.atualizar(1L, atualizacao);

            assertThat(detalhe.nome()).isEqualTo("Arroz 5kg Tio João");
            assertThat(detalhe.precoUnitario()).isEqualTo(18.5);
            assertThat(detalhe.unidadeMedida()).isEqualTo(UnidadeMedida.KG);
            assertThat(detalhe.estoque()).isEqualTo(12.5);
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException ao atualizar para um nome pertencente a outro item")
        void atualizarItem_comNomeDuplicado_lancaConflitoDadosException() {
            Item itemExistente = criarItemTeste();

            when(itemRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.of(itemExistente));
            when(itemRepository.existsByNomeIgnoreCaseAndIdNot("Feijão 1kg", 1L)).thenReturn(true);

            var atualizacao =
                    new ItemDTO.Atualizacao(
                            "Feijão 1kg",
                            1L,
                            10.0,
                            UnidadeMedida.KG,
                            "Descrição");

            assertThatThrownBy(() -> itemService.atualizar(1L, atualizacao))
                    .isInstanceOf(ConflitoDadosException.class)
                    .hasMessageContaining("Já existe um item cadastrado com o nome");

            verify(itemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao tentar atualizar item inexistente")
        void atualizarItem_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            var atualizacao =
                    new ItemDTO.Atualizacao(
                            "Item Inexistente",
                            1L,
                            12.0,
                            UnidadeMedida.UN,
                            "Descrição");

            when(itemRepository.findByIdAndDataInativoIsNull(9999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.atualizar(9999L, atualizacao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class)
                    .hasMessageContaining("Item não encontrado");
        }
    }

    @Nested
    @DisplayName("Listar Todos os Itens")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar apenas ativos quando incluirInativos for false")
        void listarTodos_somenteAtivos_retornaLista() {
            Item item = criarItemTeste();
            when(itemRepository.findAllByDataInativoIsNull()).thenReturn(List.of(item));
            when(itemRepository.calcularEstoqueAtual(1L)).thenReturn(5.0);

            List<ItemDTO.Resumo> resumo = itemService.listarTodos(false);

            assertThat(resumo).hasSize(1);
            assertThat(resumo.get(0).nome()).isEqualTo("Arroz 5kg");
            verify(itemRepository).findAllByDataInativoIsNull();
        }

        @Test
        @DisplayName("Deve retornar todos os itens incluindo inativos quando incluirInativos for true")
        void listarTodos_comInativos_retornaListaCompleta() {
            Item item = criarItemTeste();
            when(itemRepository.findAll()).thenReturn(List.of(item));
            when(itemRepository.calcularEstoqueAtual(1L)).thenReturn(5.0);

            List<ItemDTO.Resumo> resumo = itemService.listarTodos(true);

            assertThat(resumo).hasSize(1);
            verify(itemRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Buscar Item por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhe do item quando ele existir e estiver ativo")
        void buscarPorId_existente_retornaDetalhe() {
            Item itemExistente = criarItemTeste();

            when(itemRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.of(itemExistente));
            when(itemRepository.calcularEstoqueAtual(1L)).thenReturn(7.0);

            ItemDTO.Detalhe detalhe = itemService.buscarPorId(1L);

            assertThat(detalhe.id()).isEqualTo(1L);
            assertThat(detalhe.nome()).isEqualTo("Arroz 5kg");
            assertThat(detalhe.estoque()).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException se ID for inexistente")
        void buscarPorId_inexistente_lancaEntidadeNaoEncontradaException() {
            when(itemRepository.findByIdAndDataInativoIsNull(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.buscarPorId(999L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class)
                    .hasMessageContaining("Item não encontrado");
        }
    }

    @Nested
    @DisplayName("Desativar e Reativar Item")
    class DesativarEReativar {

        @Test
        @DisplayName("Deve desativar e reativar um item com sucesso")
        void desativarEReativarItem_sucesso() {
            Item item = criarItemTeste();

            when(itemRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.of(item));
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
            when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

            itemService.inativar(1L);
            assertThat(item.getDataInativo()).isNotNull();

            itemService.reativar(1L);
            assertThat(item.getDataInativo()).isNull();
        }
    }
}