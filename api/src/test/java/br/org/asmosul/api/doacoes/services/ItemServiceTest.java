package br.org.asmosul.api.doacoes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.doacoes.dtos.ItemDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.CategoriaItemRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.pessoas.models.Uf;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ItemService")
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private CategoriaItemRepository categoriaItemRepository;
    @Mock private EstoqueRepository estoqueRepository;

    @InjectMocks private ItemService itemService;

    private CategoriaItem criarCategoria(Long id, String nome) {
        CategoriaItem categoria = new CategoriaItem(nome, "Descrição");
        ReflectionTestUtils.setField(categoria, "id", id);
        return categoria;
    }

    private Item criarItem(Long id, String nome, CategoriaItem categoria) {
        Item item = new Item(nome, categoria, 1500, UnidadeMedida.PACOTE, "Descrição do item");
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    @Nested
    @DisplayName("Cadastrar Item")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar item com sucesso quando dados válidos")
        void cadastrar_comDadosValidos_cadastraERetornaDetalhe() {
            CategoriaItem categoria = criarCategoria(1L, "Alimentos");
            Item itemSalvo = criarItem(10L, "Arroz 5kg", categoria);

            var requisicao =
                    new ItemDTO.Requisicao(
                            "Arroz 5kg", 1L, 1500, UnidadeMedida.PACOTE, "Descrição do item");

            when(itemRepository.existsByNomeIgnoreCase("Arroz 5kg")).thenReturn(false);
            when(categoriaItemRepository.findByIdAndDataInativoIsNull(1L))
                    .thenReturn(Optional.of(categoria));
            when(itemRepository.save(any(Item.class))).thenReturn(itemSalvo);

            ItemDTO.Detalhe resultado = itemService.cadastrar(requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(10L);
            assertThat(resultado.nome()).isEqualTo("Arroz 5kg");
            assertThat(resultado.estoque()).isEqualTo(0);
            assertThat(resultado.locais()).isEmpty();
            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException quando nome for duplicado (RN23)")
        void cadastrar_comNomeDuplicado_lancaConflitoDadosException() {
            var requisicao =
                    new ItemDTO.Requisicao(
                            "Feijão Preto", 1L, 800, UnidadeMedida.PACOTE, "Feijão");

            when(itemRepository.existsByNomeIgnoreCase("Feijão Preto")).thenReturn(true);

            assertThatThrownBy(() -> itemService.cadastrar(requisicao))
                    .isInstanceOf(ConflitoDadosException.class);
        }

        @Test
        @DisplayName("Deve lançar ValidationException quando categoria não existir ou for inativa")
        void cadastrar_comCategoriaInexistente_lancaValidationException() {
            var requisicao =
                    new ItemDTO.Requisicao(
                            "Macarrão", 99L, 400, UnidadeMedida.PACOTE, "Macarrão");

            when(itemRepository.existsByNomeIgnoreCase("Macarrão")).thenReturn(false);
            when(categoriaItemRepository.findByIdAndDataInativoIsNull(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.cadastrar(requisicao))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("Buscar Item por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes do item com locais de armazenamento e saldo total (US-63)")
        void buscarPorId_comIdExistente_retornaDetalheComLocaisEEstoqueTotal() {
            CategoriaItem categoria = criarCategoria(1L, "Alimentos");
            Item item = criarItem(10L, "Farinha de Trigo", categoria);

            EnderecoArmazenamento endereco1 =
                    new EnderecoArmazenamento(
                            "Galpão Central",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);
            ReflectionTestUtils.setField(endereco1, "id", 100L);

            EnderecoArmazenamento endereco2 =
                    new EnderecoArmazenamento(
                            "Depósito Norte",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Norte",
                            "Rua 2",
                            "200",
                            null,
                            null);
            ReflectionTestUtils.setField(endereco2, "id", 101L);

            Estoque estoque1 = new Estoque(item, endereco1, 40);
            Estoque estoque2 = new Estoque(item, endereco2, 60);

            when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(estoqueRepository.findAllByItemIdAndQuantidadeGreaterThan(10L, 0))
                    .thenReturn(List.of(estoque1, estoque2));

            ItemDTO.Detalhe resultado = itemService.buscarPorId(10L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(10L);
            assertThat(resultado.nome()).isEqualTo("Farinha de Trigo");
            assertThat(resultado.estoque()).isEqualTo(100);
            assertThat(resultado.locais()).hasSize(2);
            assertThat(resultado.locais().get(0).idEndereco()).isEqualTo(100L);
            assertThat(resultado.locais().get(0).nomeLocalArmazenamento()).isEqualTo("Galpão Central");
            assertThat(resultado.locais().get(0).quantidade()).isEqualTo(40);
            assertThat(resultado.locais().get(1).idEndereco()).isEqualTo(101L);
            assertThat(resultado.locais().get(1).nomeLocalArmazenamento()).isEqualTo("Depósito Norte");
            assertThat(resultado.locais().get(1).quantidade()).isEqualTo(60);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando item não existir")
        void buscarPorId_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(itemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.buscarPorId(999L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Atualizar Item")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar item com sucesso")
        void atualizar_comDadosValidos_atualizaERetornaDetalhe() {
            CategoriaItem categoria = criarCategoria(1L, "Alimentos");
            Item item = criarItem(15L, "Nome Antigo", categoria);

            var requisicao =
                    new ItemDTO.Atualizacao(
                            "Nome Novo", 1L, 2000, UnidadeMedida.PACOTE, "Nova descrição");

            when(itemRepository.findByIdAndDataInativoIsNull(15L)).thenReturn(Optional.of(item));
            when(itemRepository.existsByNomeIgnoreCaseAndIdNot("Nome Novo", 15L)).thenReturn(false);
            when(categoriaItemRepository.findByIdAndDataInativoIsNull(1L))
                    .thenReturn(Optional.of(categoria));
            when(estoqueRepository.findAllByItemIdAndQuantidadeGreaterThan(15L, 0))
                    .thenReturn(List.of());

            ItemDTO.Detalhe resultado = itemService.atualizar(15L, requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.nome()).isEqualTo("Nome Novo");
            assertThat(item.getNome()).isEqualTo("Nome Novo");
            assertThat(item.getPrecoUnitario()).isEqualTo(2000);
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException ao atualizar para nome duplicado")
        void atualizar_comNomeDuplicado_lancaConflitoDadosException() {
            CategoriaItem categoria = criarCategoria(1L, "Alimentos");
            Item item = criarItem(15L, "Item 1", categoria);

            var requisicao =
                    new ItemDTO.Atualizacao(
                            "Item Já Existente", 1L, 1000, UnidadeMedida.UNIDADE, "");

            when(itemRepository.findByIdAndDataInativoIsNull(15L)).thenReturn(Optional.of(item));
            when(itemRepository.existsByNomeIgnoreCaseAndIdNot("Item Já Existente", 15L))
                    .thenReturn(true);

            assertThatThrownBy(() -> itemService.atualizar(15L, requisicao))
                    .isInstanceOf(ConflitoDadosException.class);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando item inativo ou inexistente")
        void atualizar_comItemInexistente_lancaEntidadeNaoEncontradaException() {
            var requisicao =
                    new ItemDTO.Atualizacao("Item", 1L, 100, UnidadeMedida.UNIDADE, "");

            when(itemRepository.findByIdAndDataInativoIsNull(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.atualizar(99L, requisicao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Desativar e Reativar Item")
    class DesativarEReativar {

        @Test
        @DisplayName("Deve desativar item ativo com sucesso")
        void desativar_comItemAtivo_desativaComSucesso() {
            CategoriaItem categoria = criarCategoria(1L, "Alimentos");
            Item item = criarItem(20L, "Item Para Desativar", categoria);

            when(itemRepository.findByIdAndDataInativoIsNull(20L)).thenReturn(Optional.of(item));

            itemService.desativar(20L);

            assertThat(item.isAtivo()).isFalse();
            assertThat(item.getDataInativo()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao desativar item já inativo")
        void desativar_comItemJaInativo_lancaEntidadeNaoEncontradaException() {
            when(itemRepository.findByIdAndDataInativoIsNull(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.desativar(20L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }

        @Test
        @DisplayName("Deve reativar item com sucesso")
        void reativar_comItemInativo_reativaComSucesso() {
            CategoriaItem categoria = criarCategoria(1L, "Alimentos");
            Item item = criarItem(20L, "Item Desativado", categoria);
            item.desativar();

            when(itemRepository.findById(20L)).thenReturn(Optional.of(item));

            itemService.reativar(20L);

            assertThat(item.isAtivo()).isTrue();
            assertThat(item.getDataInativo()).isNull();
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao reativar item inexistente")
        void reativar_comItemInexistente_lancaEntidadeNaoEncontradaException() {
            when(itemRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> itemService.reativar(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }
}
