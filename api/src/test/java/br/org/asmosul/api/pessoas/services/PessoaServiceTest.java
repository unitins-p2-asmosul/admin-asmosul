package br.org.asmosul.api.pessoas.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.pessoas.dtos.PessoaDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.models.Comorbidade;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.time.LocalDate;
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
@DisplayName("Testes Unitários - PessoaService")
class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ComorbidadeRepository comorbidadeRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private PessoaService pessoaService;

    private Pessoa pessoa;

    private PessoaDTO.Requisicao requisicao;

    @BeforeEach
    void setUp() {

        requisicao = new PessoaDTO.Requisicao(
                "João da Silva",
                "12345678901",
                LocalDate.of(2000, 1, 1),
                null,
                "63999999999",
                "joao@email.com",
                null,
                "Professor",
                null,
                List.of(),
                List.of(),
                "Pessoa cadastrada para teste"
        );

        pessoa = requisicao.paraEntidade();
        pessoa.setId(1L);
    }

    @Test
    @DisplayName("Deve cadastrar uma pessoa com sucesso")
    void deveCadastrarPessoaComSucesso() {

        when(pessoaRepository.existsByCpf(requisicao.cpf()))
                .thenReturn(false);

        when(pessoaRepository.existsByEmail(requisicao.email()))
                .thenReturn(false);

        when(pessoaRepository.save(any(Pessoa.class)))
                .thenReturn(pessoa);

        PessoaDTO.Detalhe resultado =
                pessoaService.cadastrar(requisicao);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("João da Silva", resultado.nome());
        assertEquals("12345678901", resultado.cpf());

        verify(pessoaRepository).existsByCpf(requisicao.cpf());
        verify(pessoaRepository).existsByEmail(requisicao.email());
        verify(pessoaRepository).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve impedir cadastro com CPF duplicado")
    void deveImpedirCadastroComCpfDuplicado() {

        when(pessoaRepository.existsByCpf(requisicao.cpf()))
                .thenReturn(true);

        assertThrows(
                ConflitoDadosException.class,
                () -> pessoaService.cadastrar(requisicao)
        );

        verify(pessoaRepository).existsByCpf(requisicao.cpf());

        verify(pessoaRepository, never())
                .save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve impedir cadastro com e-mail duplicado")
    void deveImpedirCadastroComEmailDuplicado() {

        when(pessoaRepository.existsByCpf(requisicao.cpf()))
                .thenReturn(false);

        when(pessoaRepository.existsByEmail(requisicao.email()))
                .thenReturn(true);

        assertThrows(
                ConflitoDadosException.class,
                () -> pessoaService.cadastrar(requisicao)
        );

        verify(pessoaRepository).existsByCpf(requisicao.cpf());
        verify(pessoaRepository).existsByEmail(requisicao.email());

        verify(pessoaRepository, never())
                .save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve buscar pessoa ativa pelo ID")
    void deveBuscarPessoaAtivaPorId() {

        when(pessoaRepository.findByIdAndDataInativoIsNull(1L))
                .thenReturn(Optional.of(pessoa));

        PessoaDTO.Detalhe resultado =
                pessoaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("João da Silva", resultado.nome());

        verify(pessoaRepository)
                .findByIdAndDataInativoIsNull(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar pessoa inexistente")
    void deveLancarExcecaoAoBuscarPessoaInexistente() {

        when(pessoaRepository.findByIdAndDataInativoIsNull(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> pessoaService.buscarPorId(999L)
        );

        verify(pessoaRepository)
                .findByIdAndDataInativoIsNull(999L);
    }

    @Test
    @DisplayName("Deve desativar uma pessoa")
    void deveDesativarPessoa() {

        when(pessoaRepository.findByIdAndDataInativoIsNull(1L))
                .thenReturn(Optional.of(pessoa));

        assertNull(pessoa.getDataInativo());

        pessoaService.desativar(1L);

        assertNotNull(pessoa.getDataInativo());

        verify(pessoaRepository)
                .findByIdAndDataInativoIsNull(1L);
    }

    @Test
    @DisplayName("Deve reativar uma pessoa")
    void deveReativarPessoa() {

        pessoa.setDataInativo(
                java.time.LocalDateTime.now()
        );

        when(pessoaRepository.findById(1L))
                .thenReturn(Optional.of(pessoa));

        assertNotNull(pessoa.getDataInativo());

        pessoaService.reativar(1L);

        assertNull(pessoa.getDataInativo());

        verify(pessoaRepository)
                .findById(1L);
    }

    @Test
    @DisplayName("Deve excluir uma pessoa existente")
    void deveExcluirPessoaExistente() {

        when(pessoaRepository.findById(1L))
                .thenReturn(Optional.of(pessoa));

        pessoaService.excluir(1L);

        verify(pessoaRepository).findById(1L);
        verify(pessoaRepository).delete(pessoa);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir pessoa inexistente")
    void deveLancarExcecaoAoExcluirPessoaInexistente() {

        when(pessoaRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> pessoaService.excluir(999L)
        );

        verify(pessoaRepository).findById(999L);

        verify(pessoaRepository, never())
                .delete(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve impedir cadastro com comorbidade inexistente")
    void deveImpedirCadastroComComorbidadeInexistente() {

        PessoaDTO.Requisicao requisicaoComorbidade =
                new PessoaDTO.Requisicao(
                        "João da Silva",
                        "12345678901",
                        LocalDate.of(2000, 1, 1),
                        null,
                        "63999999999",
                        "joao@email.com",
                        null,
                        "Professor",
                        null,
                        List.of(999L),
                        List.of(),
                        "Teste"
                );

        when(pessoaRepository.existsByCpf(requisicaoComorbidade.cpf()))
                .thenReturn(false);

        when(pessoaRepository.existsByEmail(requisicaoComorbidade.email()))
                .thenReturn(false);

        when(comorbidadeRepository.findAllById(
                requisicaoComorbidade.comorbidades()))
                .thenReturn(List.of());

        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> pessoaService.cadastrar(requisicaoComorbidade)
        );

        verify(comorbidadeRepository)
                .findAllById(requisicaoComorbidade.comorbidades());

        verify(pessoaRepository, never())
                .save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve impedir cadastro com categoria inexistente")
    void deveImpedirCadastroComCategoriaInexistente() {

        PessoaDTO.Requisicao requisicaoCategoria =
                new PessoaDTO.Requisicao(
                        "João da Silva",
                        "12345678901",
                        LocalDate.of(2000, 1, 1),
                        null,
                        "63999999999",
                        "joao@email.com",
                        null,
                        "Professor",
                        null,
                        List.of(),
                        List.of(999L),
                        "Teste"
                );

        when(pessoaRepository.existsByCpf(requisicaoCategoria.cpf()))
                .thenReturn(false);

        when(pessoaRepository.existsByEmail(requisicaoCategoria.email()))
                .thenReturn(false);

        when(categoriaRepository.findAllById(
                requisicaoCategoria.categorias()))
                .thenReturn(List.of());

        assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> pessoaService.cadastrar(requisicaoCategoria)
        );

        verify(categoriaRepository)
                .findAllById(requisicaoCategoria.categorias());

        verify(pessoaRepository, never())
                .save(any(Pessoa.class));
    }
}