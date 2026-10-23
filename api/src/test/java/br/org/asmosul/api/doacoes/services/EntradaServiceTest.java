package br.org.asmosul.api.doacoes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.doacoes.dtos.EntradaDTO;
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Entrada;
import br.org.asmosul.api.doacoes.models.EntradaEstoque;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaEstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.EntradaRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
import br.org.asmosul.api.doacoes.repositories.ItemRepository;
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
@DisplayName("Testes Unitários - EntradaService")
class EntradaServiceTest {

    @Mock private EntradaRepository entradaRepository;
    @Mock private EntradaEstoqueRepository entradaEstoqueRepository;
    @Mock private EstoqueRepository estoqueRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    @Mock private PessoaRepository pessoaRepository;

    @InjectMocks private EntradaService entradaService;

    private Pessoa doador;
    private Item item;
    private EnderecoArmazenamento endereco;

    @BeforeEach
    void setUp() {
        doador =
                Pessoa.criarMinima(
                        "Doador Teste", "12345678901", TipoPessoa.FISICA, "63999999999");
        doador.setId(1L);

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
    }

    @Test
    @DisplayName("Cadastro deve incrementar estoque existente e marcar pessoa como doadora")
    void cadastrar_comEstoqueExistente_incrementaSaldoEMarcaDoador() {
        Estoque estoque = new Estoque(item, endereco, 8);

        when(pessoaRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.of(doador));
        when(itemRepository.findByIdAndDataInativoIsNull(3L)).thenReturn(Optional.of(item));
        when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(4L))
                .thenReturn(Optional.of(endereco));
        when(entradaRepository.save(any(Entrada.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(entradaEstoqueRepository.save(any(EntradaEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.of(estoque));

        var requisicao =
                new EntradaDTO.Requisicao(
                        1L,
                        "Doação",
                        List.of(new EntradaDTO.ItemEntradaRequisicao(3L, 4L, 5)));

        entradaService.cadastrar(requisicao);

        assertThat(estoque.getQuantidade()).isEqualTo(13);
        assertThat(doador.isEhDoador()).isTrue();
        verify(estoqueRepository).save(estoque);
        verify(pessoaRepository).save(doador);
    }

    @Test
    @DisplayName("Cadastro deve criar estoque quando item e endereço ainda não possuírem saldo")
    void cadastrar_semEstoqueExistente_criaRegistroComQuantidadeDaEntrada() {
        when(pessoaRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.of(doador));
        when(itemRepository.findByIdAndDataInativoIsNull(3L)).thenReturn(Optional.of(item));
        when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(4L))
                .thenReturn(Optional.of(endereco));
        when(entradaRepository.save(any(Entrada.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(entradaEstoqueRepository.save(any(EntradaEstoque.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.empty());

        var requisicao =
                new EntradaDTO.Requisicao(
                        1L,
                        null,
                        List.of(new EntradaDTO.ItemEntradaRequisicao(3L, 4L, 9)));

        entradaService.cadastrar(requisicao);

        verify(estoqueRepository)
                .save(
                        org.mockito.ArgumentMatchers.argThat(
                                estoque ->
                                        estoque.getItem() == item
                                                && estoque.getArmazenamento() == endereco
                                                && estoque.getQuantidade() == 9));
    }

    @Test
    @DisplayName("Delete de entrada deve estornar saldo sem permitir quantidade negativa (RN21)")
    void deletar_quandoEntradaMaiorQueEstoque_zeraSaldo() {
        Entrada entrada = new Entrada(doador, LocalDateTime.now(), "Entrada");
        entrada.setId(10L);
        EntradaEstoque entradaEstoque = new EntradaEstoque(item, endereco, entrada, 8);
        entradaEstoque.setId(11L);
        Estoque estoque = new Estoque(item, endereco, 5);

        when(entradaRepository.findById(10L)).thenReturn(Optional.of(entrada));
        when(entradaEstoqueRepository.findAllByEntradaIdComRelacionamentos(10L))
                .thenReturn(List.of(entradaEstoque));
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.of(estoque));

        entradaService.deletar(10L);

        assertThat(estoque.getQuantidade()).isZero();
        verify(estoqueRepository).save(estoque);
        verify(entradaEstoqueRepository).deleteAll(List.of(entradaEstoque));
        verify(entradaRepository).delete(entrada);
    }

    @Test
    @DisplayName("Delete individual deve continuar mesmo quando o registro de estoque não existir")
    void deletarItemEntrada_semEstoqueExistente_removeRegistroSemFalhar() {
        Entrada entrada = new Entrada(doador, LocalDateTime.now(), "Entrada");
        entrada.setId(10L);
        EntradaEstoque entradaEstoque = new EntradaEstoque(item, endereco, entrada, 3);
        entradaEstoque.setId(11L);

        when(entradaRepository.findById(10L)).thenReturn(Optional.of(entrada));
        when(entradaEstoqueRepository.findByIdAndEntradaIdComRelacionamentos(11L, 10L))
                .thenReturn(Optional.of(entradaEstoque));
        when(estoqueRepository.findByItemIdAndArmazenamentoId(3L, 4L))
                .thenReturn(Optional.empty());

        entradaService.deletarItemEntrada(10L, 11L);

        verify(entradaEstoqueRepository).delete(entradaEstoque);
    }
}
