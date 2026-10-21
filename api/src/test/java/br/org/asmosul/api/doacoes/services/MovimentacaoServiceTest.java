package br.org.asmosul.api.doacoes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.doacoes.dtos.MovimentacaoDTO;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.Movimentacao;
import br.org.asmosul.api.doacoes.models.MovimentacaoEstoque;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.MovimentacaoEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.MovimentacaoRepository;
import br.org.asmosul.api.pessoas.models.Uf;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - MovimentacaoService")
class MovimentacaoServiceTest {

    @Mock private MovimentacaoRepository movimentacaoRepository;
    @Mock private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    @Mock private EstoqueRepository estoqueRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    @InjectMocks private MovimentacaoService movimentacaoService;

    private EnderecoArmazenamento criarEnderecoExemplo(Long id, String nome) {
        EnderecoArmazenamento endereco =
                new EnderecoArmazenamento(
                        nome,
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Centro",
                        "Av. JK",
                        "100",
                        "Sala 1",
                        "Depósito");
        ReflectionTestUtils.setField(endereco, "id", id);
        return endereco;
    }

    private Item criarItemExemplo(Long id, String nome) {
        Item item = new Item(nome, null, 10, UnidadeMedida.UNIDADE, "Descrição do item");
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    private Estoque criarEstoqueExemplo(Long id, Item item, EnderecoArmazenamento arm, Integer qtd) {
        Estoque estoque = new Estoque(item, arm, qtd);
        ReflectionTestUtils.setField(estoque, "id", id);
        return estoque;
    }

    private Movimentacao criarMovimentacaoExemplo(Long id, EnderecoArmazenamento destino, String desc) {
        Movimentacao mov = new Movimentacao(destino, LocalDateTime.now(), desc);
        ReflectionTestUtils.setField(mov, "id", id);
        return mov;
    }

    @Nested
    @DisplayName("Cadastrar Movimentação de Estoque")
    class Cadastrar {

        @Test
        @DisplayName("Deve realizar transferência atômica com upsert no destino e abatimento na origem")
        void cadastrar_comDadosValidosEUpsertDestino_transfereSaldosERetornaDetalhe() {
            EnderecoArmazenamento armOrigem = criarEnderecoExemplo(1L, "Depósito Origem");
            EnderecoArmazenamento armDestino = criarEnderecoExemplo(2L, "Depósito Destino");
            Item item = criarItemExemplo(10L, "Arroz 5kg");

            Estoque estoqueOrigem = criarEstoqueExemplo(100L, item, armOrigem, 20);

            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(10L, 1L, 5);
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Transferência para filial", List.of(itemReq));

            when(enderecoArmazenamentoRepository.findById(2L)).thenReturn(Optional.of(armDestino));
            when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(enderecoArmazenamentoRepository.findById(1L)).thenReturn(Optional.of(armOrigem));
            when(estoqueRepository.findByItemIdAndArmazenamentoId(10L, 1L)).thenReturn(Optional.of(estoqueOrigem));
            when(estoqueRepository.findByItemIdAndArmazenamentoId(10L, 2L)).thenReturn(Optional.empty());

            Movimentacao movSalva = criarMovimentacaoExemplo(1L, armDestino, "Transferência para filial");
            when(movimentacaoRepository.save(any(Movimentacao.class))).thenReturn(movSalva);

            MovimentacaoEstoque movEstoqueSalva = new MovimentacaoEstoque(item, armOrigem, movSalva, 5);
            ReflectionTestUtils.setField(movEstoqueSalva, "id", 50L);
            when(movimentacaoEstoqueRepository.saveAll(any())).thenReturn(List.of(movEstoqueSalva));

            MovimentacaoDTO.Detalhe detalhe = movimentacaoService.cadastrar(requisicao);

            assertThat(detalhe).isNotNull();
            assertThat(detalhe.id()).isEqualTo(1L);
            assertThat(detalhe.idNovoArmazenamento()).isEqualTo(2L);
            assertThat(detalhe.nomeNovoArmazenamento()).isEqualTo("Depósito Destino");
            assertThat(detalhe.itens()).hasSize(1);
            assertThat(detalhe.itens().get(0).quantidade()).isEqualTo(5);

            assertThat(estoqueOrigem.getQuantidade()).isEqualTo(15);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Collection<Estoque>> estoquesCaptor = ArgumentCaptor.forClass(Collection.class);
            verify(estoqueRepository).saveAll(estoquesCaptor.capture());
            Collection<Estoque> estoquesSalvos = estoquesCaptor.getValue();
            assertThat(estoquesSalvos).hasSize(2);

            Estoque destinoSalvo = estoquesSalvos.stream()
                    .filter(e -> e.getArmazenamento().getId().equals(2L))
                    .findFirst()
                    .orElseThrow();
            assertThat(destinoSalvo.getQuantidade()).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve somar saldo no destino caso o registro de estoque já exista")
        void cadastrar_comEstoqueDestinoJaExistente_acrescentaSaldo() {
            EnderecoArmazenamento armOrigem = criarEnderecoExemplo(1L, "Depósito Origem");
            EnderecoArmazenamento armDestino = criarEnderecoExemplo(2L, "Depósito Destino");
            Item item = criarItemExemplo(10L, "Feijão 1kg");

            Estoque estoqueOrigem = criarEstoqueExemplo(100L, item, armOrigem, 30);
            Estoque estoqueDestino = criarEstoqueExemplo(101L, item, armDestino, 10);

            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(10L, 1L, 8);
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Transferência regular", List.of(itemReq));

            when(enderecoArmazenamentoRepository.findById(2L)).thenReturn(Optional.of(armDestino));
            when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(enderecoArmazenamentoRepository.findById(1L)).thenReturn(Optional.of(armOrigem));
            when(estoqueRepository.findByItemIdAndArmazenamentoId(10L, 1L)).thenReturn(Optional.of(estoqueOrigem));
            when(estoqueRepository.findByItemIdAndArmazenamentoId(10L, 2L)).thenReturn(Optional.of(estoqueDestino));

            Movimentacao movSalva = criarMovimentacaoExemplo(1L, armDestino, "Transferência regular");
            when(movimentacaoRepository.save(any(Movimentacao.class))).thenReturn(movSalva);

            MovimentacaoEstoque movEstoqueSalva = new MovimentacaoEstoque(item, armOrigem, movSalva, 8);
            ReflectionTestUtils.setField(movEstoqueSalva, "id", 51L);
            when(movimentacaoEstoqueRepository.saveAll(any())).thenReturn(List.of(movEstoqueSalva));

            MovimentacaoDTO.Detalhe detalhe = movimentacaoService.cadastrar(requisicao);

            assertThat(detalhe).isNotNull();
            assertThat(estoqueOrigem.getQuantidade()).isEqualTo(22);
            assertThat(estoqueDestino.getQuantidade()).isEqualTo(18);
        }

        @Test
        @DisplayName("Deve lançar ValidationException quando o endereço de origem for idêntico ao de destino")
        void cadastrar_comOrigemIgualADestino_lancaValidationException() {
            EnderecoArmazenamento arm = criarEnderecoExemplo(1L, "Depósito Único");
            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(10L, 1L, 5);
            var requisicao = new MovimentacaoDTO.Requisicao(1L, "Transferência inválida", List.of(itemReq));

            when(enderecoArmazenamentoRepository.findById(1L)).thenReturn(Optional.of(arm));

            assertThatThrownBy(() -> movimentacaoService.cadastrar(requisicao))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Dados inválidos");

            verify(movimentacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar ValidationException quando o saldo de origem for insuficiente (RN11)")
        void cadastrar_comSaldoInsuficienteNaOrigem_lancaValidationException() {
            EnderecoArmazenamento armOrigem = criarEnderecoExemplo(1L, "Depósito Origem");
            EnderecoArmazenamento armDestino = criarEnderecoExemplo(2L, "Depósito Destino");
            Item item = criarItemExemplo(10L, "Óleo de Soja");

            Estoque estoqueOrigem = criarEstoqueExemplo(100L, item, armOrigem, 3); // Apenas 3 em estoque

            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(10L, 1L, 5); // Tenta mover 5
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Tentativa de mover mais que o saldo", List.of(itemReq));

            when(enderecoArmazenamentoRepository.findById(2L)).thenReturn(Optional.of(armDestino));
            when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(enderecoArmazenamentoRepository.findById(1L)).thenReturn(Optional.of(armOrigem));
            when(estoqueRepository.findByItemIdAndArmazenamentoId(10L, 1L)).thenReturn(Optional.of(estoqueOrigem));

            assertThatThrownBy(() -> movimentacaoService.cadastrar(requisicao))
                    .isInstanceOf(ValidationException.class);

            verify(movimentacaoEstoqueRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando destino não existir (404)")
        void cadastrar_comDestinoInexistente_lancaEntidadeNaoEncontradaException() {
            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(10L, 1L, 5);
            var requisicao = new MovimentacaoDTO.Requisicao(999L, "Destino não existe", List.of(itemReq));

            when(enderecoArmazenamentoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movimentacaoService.cadastrar(requisicao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando item não existir (404)")
        void cadastrar_comItemInexistente_lancaEntidadeNaoEncontradaException() {
            EnderecoArmazenamento armDestino = criarEnderecoExemplo(2L, "Destino");
            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(999L, 1L, 5);
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Item não existe", List.of(itemReq));

            when(enderecoArmazenamentoRepository.findById(2L)).thenReturn(Optional.of(armDestino));
            when(itemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movimentacaoService.cadastrar(requisicao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando origem não existir (404)")
        void cadastrar_comOrigemInexistente_lancaEntidadeNaoEncontradaException() {
            EnderecoArmazenamento armDestino = criarEnderecoExemplo(2L, "Destino");
            Item item = criarItemExemplo(10L, "Macarrão");

            var itemReq = new MovimentacaoDTO.ItemMovimentacaoRequisicao(10L, 999L, 5);
            var requisicao = new MovimentacaoDTO.Requisicao(2L, "Origem não existe", List.of(itemReq));

            when(enderecoArmazenamentoRepository.findById(2L)).thenReturn(Optional.of(armDestino));
            when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
            when(enderecoArmazenamentoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movimentacaoService.cadastrar(requisicao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Atualizar Movimentação de Estoque")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar restritamente a descrição da movimentação")
        void atualizar_comDadosValidos_atualizaDescricaoERetornaDetalhe() {
            EnderecoArmazenamento armDestino = criarEnderecoExemplo(2L, "Depósito Destino");
            Movimentacao mov = criarMovimentacaoExemplo(1L, armDestino, "Descrição antiga");
            Item item = criarItemExemplo(10L, "Arroz");
            EnderecoArmazenamento armOrigem = criarEnderecoExemplo(1L, "Depósito Origem");
            MovimentacaoEstoque movEstoque = new MovimentacaoEstoque(item, armOrigem, mov, 5);
            ReflectionTestUtils.setField(movEstoque, "id", 100L);

            when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(mov));
            when(movimentacaoEstoqueRepository.findAllByMovimentacaoId(1L)).thenReturn(List.of(movEstoque));

            var atualizacao = new MovimentacaoDTO.Atualizacao("Nova descrição atualizada");
            MovimentacaoDTO.Detalhe resultado = movimentacaoService.atualizar(1L, atualizacao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.descricao()).isEqualTo("Nova descrição atualizada");
            assertThat(mov.getDescricao()).isEqualTo("Nova descrição atualizada");
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao atualizar movimentação inexistente")
        void atualizar_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(movimentacaoRepository.findById(999L)).thenReturn(Optional.empty());

            var atualizacao = new MovimentacaoDTO.Atualizacao("Descrição");
            assertThatThrownBy(() -> movimentacaoService.atualizar(999L, atualizacao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Buscar Movimentação por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes da movimentação quando existir")
        void buscarPorId_comIdExistente_retornaDetalhe() {
            EnderecoArmazenamento armDestino = criarEnderecoExemplo(2L, "Depósito Destino");
            Movimentacao mov = criarMovimentacaoExemplo(1L, armDestino, "Movimentação 1");
            Item item = criarItemExemplo(10L, "Arroz");
            EnderecoArmazenamento armOrigem = criarEnderecoExemplo(1L, "Depósito Origem");
            MovimentacaoEstoque movEstoque = new MovimentacaoEstoque(item, armOrigem, mov, 10);
            ReflectionTestUtils.setField(movEstoque, "id", 200L);

            when(movimentacaoRepository.findById(1L)).thenReturn(Optional.of(mov));
            when(movimentacaoEstoqueRepository.findAllByMovimentacaoId(1L)).thenReturn(List.of(movEstoque));

            MovimentacaoDTO.Detalhe detalhe = movimentacaoService.buscarPorId(1L);

            assertThat(detalhe).isNotNull();
            assertThat(detalhe.id()).isEqualTo(1L);
            assertThat(detalhe.itens()).hasSize(1);
            assertThat(detalhe.itens().get(0).nomeItem()).isEqualTo("Arroz");
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando não existir")
        void buscarPorId_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(movimentacaoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movimentacaoService.buscarPorId(999L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Listar Movimentações")
    class Listar {

        @Test
        @DisplayName("Deve listar movimentações paginadas mapeando itens e resumos")
        void listar_comPaginacao_retornaRespostaPaginada() {
            EnderecoArmazenamento armDestino = criarEnderecoExemplo(2L, "Depósito Destino");
            Movimentacao mov1 = criarMovimentacaoExemplo(1L, armDestino, "Mov 1");
            Movimentacao mov2 = criarMovimentacaoExemplo(2L, armDestino, "Mov 2");

            Item item = criarItemExemplo(10L, "Arroz");
            EnderecoArmazenamento armOrigem = criarEnderecoExemplo(1L, "Depósito Origem");
            MovimentacaoEstoque me1 = new MovimentacaoEstoque(item, armOrigem, mov1, 5);
            MovimentacaoEstoque me2 = new MovimentacaoEstoque(item, armOrigem, mov2, 8);

            Pageable pageable = PageRequest.of(0, 10);
            when(movimentacaoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(mov1, mov2), pageable, 2));

            when(movimentacaoEstoqueRepository.findAllByMovimentacaoIdIn(List.of(1L, 2L)))
                    .thenReturn(List.of(me1, me2));

            RespostaPaginada<MovimentacaoDTO.Resumo> resposta =
                    movimentacaoService.listar(null, pageable);

            assertThat(resposta).isNotNull();
            assertThat(resposta.dados()).hasSize(2);
            assertThat(resposta.totalElementos()).isEqualTo(2);
            assertThat(resposta.dados().get(0).nomeItem()).isEqualTo("Arroz");
            assertThat(resposta.dados().get(0).quantidade()).isEqualTo("5 un");
        }

        @Test
        @DisplayName("Deve exibir quantidades agregadas separadamente por unidade de medida")
        void listar_comItensDeUnidadesDiferentes_naoSomaUnidadesIncompativeis() {
            EnderecoArmazenamento destino = criarEnderecoExemplo(2L, "Destino");
            EnderecoArmazenamento origem = criarEnderecoExemplo(1L, "Origem");
            Movimentacao movimentacao = criarMovimentacaoExemplo(1L, destino, "Transferência");
            Item arroz = new Item("Arroz", null, 10, UnidadeMedida.QUILO, "Alimento");
            Item latas = new Item("Latas", null, 10, UnidadeMedida.UNIDADE, "Alimento");
            List<MovimentacaoEstoque> itens = List.of(
                    new MovimentacaoEstoque(arroz, origem, movimentacao, 2),
                    new MovimentacaoEstoque(latas, origem, movimentacao, 5));

            MovimentacaoDTO.Resumo resumo = MovimentacaoDTO.Resumo.deEntidade(movimentacao, itens);

            assertThat(resumo.quantidade()).isEqualTo("2 kg, 5 un");
        }

        @Test
        @DisplayName("Deve rejeitar período de filtro com data inicial posterior à final")
        void listar_comPeriodoInvertido_lancaValidationException() {
            var filtro = new MovimentacaoDTO.MovimentacaoFiltroDTO(
                    null,
                    null,
                    null,
                    null,
                    null,
                    LocalDateTime.of(2026, 10, 21, 12, 0),
                    LocalDateTime.of(2026, 10, 20, 12, 0));

            assertThatThrownBy(() ->
                            movimentacaoService.listar(filtro, PageRequest.of(0, 10)))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("Garantia de Impossibilidade de Exclusão (RN22)")
    class RegraRN22 {

        @Test
        @DisplayName("Deve assegurar que MovimentacaoService não expõe métodos de exclusão/deleção")
        void assegurar_queNaoExistemMetodosDeExclusaoNoService() {
            List<String> metodosExclusao = Arrays.stream(MovimentacaoService.class.getDeclaredMethods())
                    .map(Method::getName)
                    .filter(nome -> nome.toLowerCase().contains("delete")
                            || nome.toLowerCase().contains("excluir")
                            || nome.toLowerCase().contains("remover")
                            || nome.toLowerCase().contains("desativar"))
                    .toList();

            assertThat(metodosExclusao)
                    .as("MovimentacaoService não deve conter métodos de exclusão/desativação (RN22)")
                    .isEmpty();
        }
    }
}
