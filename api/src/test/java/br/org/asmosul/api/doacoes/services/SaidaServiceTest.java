package br.org.asmosul.api.doacoes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.doacoes.dtos.SaidaDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.Saida;
import br.org.asmosul.api.doacoes.models.SaidaEstoque;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.SaidaRepository;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.models.TipoPessoa;
import br.org.asmosul.api.pessoas.models.Uf;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.time.LocalDateTime;
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
@DisplayName("Testes Unitários - SaidaService")
class SaidaServiceTest {

    @Mock private SaidaRepository saidaRepository;
    @Mock private SaidaEstoqueRepository saidaEstoqueRepository;
    @Mock private EstoqueRepository estoqueRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    @Mock private PessoaRepository pessoaRepository;

    @InjectMocks private SaidaService saidaService;

    private Pessoa recebedor;
    private Item item;
    private EnderecoArmazenamento endereco;
    private Saida saida;

    @BeforeEach
    void setUp() {
        recebedor =
                Pessoa.criarMinima(
                        "Recebedor Teste", "12345678901", TipoPessoa.FISICA, "63999999999");
        recebedor.setId(1L);

        CategoriaItem categoria = new CategoriaItem("Alimentos", "Categoria");
        categoria.setId(2L);

        item = new Item("Arroz", categoria, 1000, UnidadeMedida.PACOTE, "Arroz");
        item.setId(3L);

        endereco =
                new EnderecoArmazenamento(
                        "Galpão",
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Centro",
                        "Avenida",
                        "100",
                        null,
                        null);
        endereco.setId(4L);

        saida = new Saida(recebedor, LocalDateTime.now(), "Saída");
        saida.setId(10L);
    }

    private void mockarEntidadesDoCadastro() {
        when(pessoaRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.of(recebedor));
        when(itemRepository.findByIdAndDataInativoIsNull(3L)).thenReturn(Optional.of(item));
        when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(4L))
                .thenReturn(Optional.of(endereco));
        when(saidaRepository.save(any(Saida.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private SaidaDTO.Requisicao requisicao(SaidaDTO.ItemSaidaRequisicao... itens) {
        return new SaidaDTO.Requisicao(1L, "Doação", List.of(itens));
    }

    @Test
    @DisplayName("Cadastro deve abater o saldo do estoque e marcar pessoa como beneficiária")
    void cadastrar_comSaldoSuficiente_abateEstoqueEMarcaBeneficiario() {
        Estoque estoque = new Estoque(item, endereco, 8);
        mockarEntidadesDoCadastro();
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.of(estoque));
        when(saidaEstoqueRepository.save(any(SaidaEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var detalhe =
                saidaService.cadastrar(requisicao(new SaidaDTO.ItemSaidaRequisicao(3L, 4L, 8)));

        assertThat(estoque.getQuantidade()).isZero();
        assertThat(recebedor.isEhBeneficiario()).isTrue();
        assertThat(detalhe.itens()).hasSize(1);
        verify(estoqueRepository).save(estoque);
        verify(pessoaRepository).save(recebedor);
    }

    @Test
    @DisplayName("Cadastro deve falhar com saldo insuficiente sem alterar estoque (RN11)")
    void cadastrar_comSaldoInsuficiente_lancaValidationException() {
        Estoque estoque = new Estoque(item, endereco, 4);
        mockarEntidadesDoCadastro();
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.of(estoque));

        assertThatThrownBy(
                        () ->
                                saidaService.cadastrar(
                                        requisicao(new SaidaDTO.ItemSaidaRequisicao(3L, 4L, 5))))
                .isInstanceOf(ValidationException.class)
                .satisfies(
                        ex ->
                                assertThat(((ValidationException) ex).getErros())
                                        .singleElement()
                                        .satisfies(
                                                erro -> {
                                                    assertThat(erro.campo())
                                                            .isEqualTo("itens[0].quantidade");
                                                    assertThat(erro.mensagem())
                                                            .contains(
                                                                    "Disponível: 4",
                                                                    "solicitado: 5");
                                                }));

        assertThat(estoque.getQuantidade()).isEqualTo(4);
        verify(estoqueRepository, never()).save(any());
        verify(saidaEstoqueRepository, never()).save(any());
        verify(pessoaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastro deve falhar quando não houver registro de estoque no endereço (RN11)")
    void cadastrar_semRegistroDeEstoque_lancaValidationException() {
        mockarEntidadesDoCadastro();
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                saidaService.cadastrar(
                                        requisicao(new SaidaDTO.ItemSaidaRequisicao(3L, 4L, 1))))
                .isInstanceOf(ValidationException.class);

        verify(estoqueRepository, never()).save(any());
        verify(saidaEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastro deve considerar a soma de itens repetidos no mesmo endereço (RN11)")
    void cadastrar_comItemRepetidoExcedendoSaldo_lancaValidationException() {
        Estoque estoque = new Estoque(item, endereco, 6);
        mockarEntidadesDoCadastro();
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.of(estoque));
        when(saidaEstoqueRepository.save(any(SaidaEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(
                        () ->
                                saidaService.cadastrar(
                                        requisicao(
                                                new SaidaDTO.ItemSaidaRequisicao(3L, 4L, 4),
                                                new SaidaDTO.ItemSaidaRequisicao(3L, 4L, 4))))
                .isInstanceOf(ValidationException.class)
                .satisfies(
                        ex ->
                                assertThat(((ValidationException) ex).getErros())
                                        .extracting(ValidationException.CampoErro::campo)
                                        .containsExactly("itens[1].quantidade"));
    }

    @Test
    @DisplayName("Cadastro deve retornar 404 quando o recebedor não existir")
    void cadastrar_comRecebedorInexistente_lancaEntidadeNaoEncontrada() {
        when(pessoaRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                saidaService.cadastrar(
                                        requisicao(new SaidaDTO.ItemSaidaRequisicao(3L, 4L, 1))))
                .isInstanceOf(EntidadeNaoEncontradaException.class);

        verify(saidaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete de saída deve estornar ao estoque todos os itens e remover em cascata")
    void deletar_estornaTodosOsItensERemoveSaida() {
        SaidaEstoque primeiro = new SaidaEstoque(item, saida, endereco, 3);
        SaidaEstoque segundo = new SaidaEstoque(item, saida, endereco, 2);
        Estoque estoque = new Estoque(item, endereco, 5);

        when(saidaRepository.findById(10L)).thenReturn(Optional.of(saida));
        when(saidaEstoqueRepository.findAllBySaidaIdComRelacionamentos(10L))
                .thenReturn(List.of(primeiro, segundo));
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.of(estoque));

        saidaService.deletar(10L);

        assertThat(estoque.getQuantidade()).isEqualTo(10);
        verify(saidaEstoqueRepository).deleteAll(List.of(primeiro, segundo));
        verify(saidaRepository).delete(saida);
    }

    @Test
    @DisplayName("Delete de saída deve recriar o estoque quando o registro não existir mais")
    void deletar_semRegistroDeEstoque_criaEstoqueComQuantidadeEstornada() {
        SaidaEstoque saidaEstoque = new SaidaEstoque(item, saida, endereco, 7);

        when(saidaRepository.findById(10L)).thenReturn(Optional.of(saida));
        when(saidaEstoqueRepository.findAllBySaidaIdComRelacionamentos(10L))
                .thenReturn(List.of(saidaEstoque));
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L)).thenReturn(Optional.empty());

        saidaService.deletar(10L);

        verify(estoqueRepository)
                .save(
                        argThat(
                                estoque ->
                                        estoque.getItem() == item
                                                && estoque.getArmazenamento() == endereco
                                                && estoque.getQuantidade() == 7));
    }

    @Test
    @DisplayName("Delete individual deve estornar a quantidade e remover o registro")
    void deletarItemSaida_comOutrosItens_estornaERemove() {
        SaidaEstoque saidaEstoque = new SaidaEstoque(item, saida, endereco, 3);
        saidaEstoque.setId(11L);
        Estoque estoque = new Estoque(item, endereco, 1);

        when(saidaRepository.findById(10L)).thenReturn(Optional.of(saida));
        when(saidaEstoqueRepository.findByIdAndSaidaIdComRelacionamentos(11L, 10L))
                .thenReturn(Optional.of(saidaEstoque));
        when(saidaEstoqueRepository.countBySaidaId(10L)).thenReturn(2L);
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.of(estoque));

        saidaService.deletarItemSaida(10L, 11L);

        assertThat(estoque.getQuantidade()).isEqualTo(4);
        verify(saidaEstoqueRepository).delete(saidaEstoque);
    }

    @Test
    @DisplayName("Delete individual do último item deve ser bloqueado sem estornar")
    void deletarItemSaida_ultimoItem_lancaValidationException() {
        SaidaEstoque saidaEstoque = new SaidaEstoque(item, saida, endereco, 3);
        saidaEstoque.setId(11L);

        when(saidaRepository.findById(10L)).thenReturn(Optional.of(saida));
        when(saidaEstoqueRepository.findByIdAndSaidaIdComRelacionamentos(11L, 10L))
                .thenReturn(Optional.of(saidaEstoque));
        when(saidaEstoqueRepository.countBySaidaId(10L)).thenReturn(1L);

        assertThatThrownBy(() -> saidaService.deletarItemSaida(10L, 11L))
                .isInstanceOf(ValidationException.class)
                .satisfies(
                        ex ->
                                assertThat(((ValidationException) ex).getErros())
                                        .singleElement()
                                        .satisfies(
                                                erro ->
                                                        assertThat(erro.mensagem())
                                                                .contains(
                                                                        "DELETE /doacoes/saidas/10")));

        verify(estoqueRepository, never()).save(any());
        verify(saidaEstoqueRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete individual deve retornar 404 quando o registro não pertencer à saída")
    void deletarItemSaida_registroDeOutraSaida_lancaEntidadeNaoEncontrada() {
        when(saidaRepository.findById(10L)).thenReturn(Optional.of(saida));
        when(saidaEstoqueRepository.findByIdAndSaidaIdComRelacionamentos(99L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> saidaService.deletarItemSaida(10L, 99L))
                .isInstanceOf(EntidadeNaoEncontradaException.class);

        verify(estoqueRepository, never()).save(any());
        verify(saidaEstoqueRepository, never()).delete(any());
    }
}
