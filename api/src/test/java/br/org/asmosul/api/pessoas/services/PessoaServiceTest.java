package br.org.asmosul.api.pessoas.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import br.org.asmosul.api.pessoas.dtos.PessoaDTO;
import br.org.asmosul.api.pessoas.dtos.PessoaFiltroDTO;
import br.org.asmosul.api.pessoas.models.*;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - PessoaService")
class PessoaServiceTest {

    @Mock private PessoaRepository pessoaRepository;
    @Mock private ComorbidadeRepository comorbidadeRepository;
    @Mock private CategoriaRepository categoriaRepository;

    @InjectMocks private PessoaService pessoaService;

    private Categoria categoriaPadrao;
    private Comorbidade comorbidadePadrao;

    @BeforeEach
    void setUp() {
        categoriaPadrao = new Categoria("Sócio Fundador", "Categoria de fundadores");
        categoriaPadrao.setId(1L);

        comorbidadePadrao = new Comorbidade("Hipertensão", "Pressão alta crônica");
        comorbidadePadrao.setId(1L);
    }

    private void assertValidationExceptionContem(ThrowingCallable callable, String trechoMensagem) {
        assertThatThrownBy(callable)
                .isInstanceOf(ValidationException.class)
                .satisfies(
                        ex -> {
                            ValidationException ve = (ValidationException) ex;
                            assertThat(ve.getErros())
                                    .anyMatch(err -> err.mensagem().contains(trechoMensagem));
                        });
    }

    private PessoaDTO.Requisicao criarRequisicaoPfValida() {
        return new PessoaDTO.Requisicao(
                "Maria Silva",
                "12345678901",
                TipoPessoa.FISICA,
                LocalDate.of(1995, 8, 19),
                Sexo.FEMININO,
                "63999998888",
                "maria.silva@email.com",
                Escolaridade.SUPERIOR_COMPLETO,
                "Assistente",
                RendaFamiliar.ENTRE_DOIS_MIl_E_TRES_MIL,
                List.of(1L),
                List.of(1L),
                "Descrição válida",
                "77000-000",
                Uf.TO,
                "Palmas",
                "Plano Diretor",
                "Rua 1",
                "Apto 101",
                2,
                true,
                false);
    }

    private PessoaDTO.Requisicao criarRequisicaoPjValida() {
        return new PessoaDTO.Requisicao(
                "Empresa Solidária LTDA",
                "12345678000199",
                TipoPessoa.JURIDICA,
                null,
                null,
                "63988887777",
                "contato@empresa.com",
                null,
                null,
                null,
                null,
                List.of(1L),
                "Doações corporativas",
                "77000-000",
                Uf.TO,
                "Palmas",
                "Centro",
                "Avenida JK",
                "Sala 200",
                0,
                false,
                true);
    }

    private Pessoa criarPessoaPfTeste() {
        return new Pessoa(
                "Carlos",
                "12345678901",
                TipoPessoa.FISICA,
                LocalDate.of(1990, 1, 1),
                Sexo.MASCULINO,
                "63999998888",
                "carlos@email.com",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                false,
                false);
    }

    @Nested
    @DisplayName("Cadastrar Pessoa - Cenários Felizes e Regras")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar Pessoa Física com sucesso (RN04, RN07)")
        void cadastrarPessoa_comPessoaFisicaValida_salvaERetornaDetalhe() {
            var req = criarRequisicaoPfValida();

            when(pessoaRepository.existsByCpfCnpj("12345678901")).thenReturn(false);
            when(pessoaRepository.existsByEmail("maria.silva@email.com")).thenReturn(false);
            when(categoriaRepository.findAllById(List.of(1L))).thenReturn(List.of(categoriaPadrao));
            when(comorbidadeRepository.findAllById(List.of(1L)))
                    .thenReturn(List.of(comorbidadePadrao));
            when(pessoaRepository.save(any(Pessoa.class)))
                    .thenAnswer(
                            inv -> {
                                Pessoa p = inv.getArgument(0);
                                p.setId(10L);
                                return p;
                            });

            PessoaDTO.Detalhe detalhe = pessoaService.cadastrar(req);

            assertThat(detalhe).isNotNull();
            assertThat(detalhe.id()).isEqualTo(10L);
            assertThat(detalhe.nome()).isEqualTo("Maria Silva");
            assertThat(detalhe.cpfCnpj()).isEqualTo("12345678901");
            assertThat(detalhe.tipoPessoa()).isEqualTo(TipoPessoa.FISICA);
            assertThat(detalhe.categorias()).hasSize(1);
            assertThat(detalhe.comorbidades()).hasSize(1);
            assertThat(detalhe.ehBeneficiario()).isTrue();
            verify(pessoaRepository).save(any(Pessoa.class));
        }

        @Test
        @DisplayName("Deve cadastrar Pessoa Jurídica com sucesso (RN04, RN05, RN06)")
        void cadastrarPessoa_comPessoaJuridicaValida_salvaERetornaDetalhe() {
            var req = criarRequisicaoPjValida();

            when(pessoaRepository.existsByCpfCnpj("12345678000199")).thenReturn(false);
            when(pessoaRepository.existsByEmail("contato@empresa.com")).thenReturn(false);
            when(categoriaRepository.findAllById(List.of(1L))).thenReturn(List.of(categoriaPadrao));
            when(pessoaRepository.save(any(Pessoa.class)))
                    .thenAnswer(
                            inv -> {
                                Pessoa p = inv.getArgument(0);
                                p.setId(20L);
                                return p;
                            });

            PessoaDTO.Detalhe detalhe = pessoaService.cadastrar(req);

            assertThat(detalhe).isNotNull();
            assertThat(detalhe.id()).isEqualTo(20L);
            assertThat(detalhe.tipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
            assertThat(detalhe.cpfCnpj()).isEqualTo("12345678000199");
            assertThat(detalhe.ehBeneficiario()).isFalse();
            assertThat(detalhe.ehDoador()).isTrue();
            verify(pessoaRepository).save(any(Pessoa.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException quando CPF/CNPJ já existir (RN03)")
        void cadastrarPessoa_comCpfCnpjDuplicado_lancaConflitoDadosException() {
            var req = criarRequisicaoPfValida();

            when(pessoaRepository.existsByCpfCnpj("12345678901")).thenReturn(true);

            assertThatThrownBy(() -> pessoaService.cadastrar(req))
                    .isInstanceOf(ConflitoDadosException.class)
                    .hasMessageContaining("Já existe uma pessoa cadastrada com este CPF/CNPJ.");

            verify(pessoaRepository, never()).save(any(Pessoa.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException quando E-mail já existir (RN03)")
        void cadastrarPessoa_comEmailDuplicado_lancaConflitoDadosException() {
            var req = criarRequisicaoPfValida();

            when(pessoaRepository.existsByCpfCnpj("12345678901")).thenReturn(false);
            when(pessoaRepository.existsByEmail("maria.silva@email.com")).thenReturn(true);

            assertThatThrownBy(() -> pessoaService.cadastrar(req))
                    .isInstanceOf(ConflitoDadosException.class)
                    .hasMessageContaining("Já existe uma pessoa cadastrada com este e-mail.");

            verify(pessoaRepository, never()).save(any(Pessoa.class));
        }

        @Test
        @DisplayName(
                "Deve lançar ValidationException se PJ for cadastrada como beneficiária (RN05)")
        void cadastrarPessoa_comPjEBeneficiarioTrue_lancaValidationException() {
            var req =
                    new PessoaDTO.Requisicao(
                            "Empresa Solidária",
                            "12345678000199",
                            TipoPessoa.JURIDICA,
                            null,
                            null,
                            "63988887777",
                            "pj@empresa.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            "Desc",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Rua 1",
                            null,
                            0,
                            true, // PROIBIDO para PJ
                            true);

            assertValidationExceptionContem(
                    () -> pessoaService.cadastrar(req),
                    "Pessoa Jurídica não pode ser cadastrada como beneficiária");

            verify(pessoaRepository, never()).save(any(Pessoa.class));
        }

        @Test
        @DisplayName("Deve lançar ValidationException se PJ não tiver CNPJ de 14 dígitos (RN06)")
        void cadastrarPessoa_comPjECnpjInvalido_lancaValidationException() {
            var req =
                    new PessoaDTO.Requisicao(
                            "Empresa Solidária",
                            "12345678901", // 11 dígitos, inválido para PJ
                            TipoPessoa.JURIDICA,
                            null,
                            null,
                            "63988887777",
                            "pj@empresa.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            "Desc",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Rua 1",
                            null,
                            0,
                            false,
                            true);

            assertValidationExceptionContem(
                    () -> pessoaService.cadastrar(req), "CNPJ deve conter exatamente 14 dígitos");
        }

        @Test
        @DisplayName("Deve lançar ValidationException se PJ não informar e-mail (RN06)")
        void cadastrarPessoa_comPjSemEmail_lancaValidationException() {
            var req =
                    new PessoaDTO.Requisicao(
                            "Empresa Solidária",
                            "12345678000199",
                            TipoPessoa.JURIDICA,
                            null,
                            null,
                            "63988887777",
                            "", // sem email
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            "Desc",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Rua 1",
                            null,
                            0,
                            false,
                            true);

            assertValidationExceptionContem(
                    () -> pessoaService.cadastrar(req),
                    "O e-mail é obrigatório para Pessoa Jurídica");
        }

        @Test
        @DisplayName("Deve lançar ValidationException se PJ não informar endereço (RN06)")
        void cadastrarPessoa_comPjSemEndereco_lancaValidationException() {
            var req =
                    new PessoaDTO.Requisicao(
                            "Empresa Solidária",
                            "12345678000199",
                            TipoPessoa.JURIDICA,
                            null,
                            null,
                            "63988887777",
                            "pj@empresa.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            "Desc",
                            "", // sem CEP
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Rua 1",
                            null,
                            0,
                            false,
                            true);

            assertValidationExceptionContem(
                    () -> pessoaService.cadastrar(req), "O CEP é obrigatório para Pessoa Jurídica");
        }

        @Test
        @DisplayName("Deve lançar ValidationException se PJ informar comorbidades (RN06)")
        void cadastrarPessoa_comPjEComorbidades_lancaValidationException() {
            var req =
                    new PessoaDTO.Requisicao(
                            "Empresa Solidária",
                            "12345678000199",
                            TipoPessoa.JURIDICA,
                            null,
                            null,
                            "63988887777",
                            "pj@empresa.com",
                            null,
                            null,
                            null,
                            List.of(1L), // comorbidade informada para PJ
                            List.of(1L),
                            "Desc",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Rua 1",
                            null,
                            0,
                            false,
                            true);

            assertValidationExceptionContem(
                    () -> pessoaService.cadastrar(req),
                    "Pessoa Jurídica não pode possuir comorbidades");
        }

        @Test
        @DisplayName("Deve lançar ValidationException se PF não tiver CPF com 11 dígitos (RN07)")
        void cadastrarPessoa_comPfECpfInvalido_lancaValidationException() {
            var req =
                    new PessoaDTO.Requisicao(
                            "Maria",
                            "12345678000199", // 14 dígitos para PF
                            TipoPessoa.FISICA,
                            LocalDate.of(1990, 1, 1),
                            Sexo.FEMININO,
                            "63999998888",
                            "maria@email.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            false,
                            false);

            assertValidationExceptionContem(
                    () -> pessoaService.cadastrar(req), "CPF deve conter exatamente 11 dígitos");
        }

        @Test
        @DisplayName("Deve lançar ValidationException se PF não informar data de nascimento (RN07)")
        void cadastrarPessoa_comPfSemDataNascimento_lancaValidationException() {
            var req =
                    new PessoaDTO.Requisicao(
                            "Maria",
                            "12345678901",
                            TipoPessoa.FISICA,
                            null, // sem data
                            Sexo.FEMININO,
                            "63999998888",
                            "maria@email.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            false,
                            false);

            assertValidationExceptionContem(
                    () -> pessoaService.cadastrar(req),
                    "A data de nascimento é obrigatória para Pessoa Física");
        }

        @Test
        @DisplayName("Deve lançar ValidationException se PF não informar categoria (RN07)")
        void cadastrarPessoa_comPfSemCategoria_lancaValidationException() {
            var req =
                    new PessoaDTO.Requisicao(
                            "Maria",
                            "12345678901",
                            TipoPessoa.FISICA,
                            LocalDate.of(1990, 1, 1),
                            Sexo.FEMININO,
                            "63999998888",
                            "maria@email.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(), // sem categoria
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            false,
                            false);

            assertValidationExceptionContem(
                    () -> pessoaService.cadastrar(req),
                    "Pessoa Física deve possuir ao menos uma categoria vinculada");
        }
    }

    @Nested
    @DisplayName("Atualizar Pessoa - Cenários e Regras")
    class Atualizar {

        @Test
        @DisplayName("Deve lançar ValidationException se tentar alterar tipoPessoa (RN08)")
        void atualizarPessoa_tentandoAlterarTipoPessoa_lancaValidationException() {
            Pessoa pessoaExistente = criarPessoaPfTeste();

            when(pessoaRepository.findByIdAndDataInativoIsNull(1L))
                    .thenReturn(Optional.of(pessoaExistente));

            var atualizacao =
                    new PessoaDTO.Atualizacao(
                            "Carlos",
                            "12345678000199",
                            TipoPessoa.JURIDICA, // TENTANDO MUDAR DE PF PARA PJ!
                            null,
                            null,
                            "63999998888",
                            "carlos@email.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            false,
                            false);

            assertValidationExceptionContem(
                    () -> pessoaService.atualizar(1L, atualizacao),
                    "O tipo de pessoa não pode ser alterado");
        }

        @Test
        @DisplayName("Deve atualizar pessoa com sucesso")
        void atualizarPessoa_comDadosValidos_atualizaERetornaDetalhe() {
            Pessoa pessoaExistente = criarPessoaPfTeste();
            pessoaExistente.setId(1L);

            when(pessoaRepository.findByIdAndDataInativoIsNull(1L))
                    .thenReturn(Optional.of(pessoaExistente));
            when(pessoaRepository.existsByCpfCnpjAndIdNot("12345678901", 1L)).thenReturn(false);
            when(pessoaRepository.existsByEmailAndIdNot("carlos.novo@email.com", 1L))
                    .thenReturn(false);
            when(categoriaRepository.findAllById(List.of(1L))).thenReturn(List.of(categoriaPadrao));

            var atualizacao =
                    new PessoaDTO.Atualizacao(
                            "Carlos Atualizado",
                            "12345678901",
                            TipoPessoa.FISICA,
                            LocalDate.of(1990, 1, 1),
                            Sexo.MASCULINO,
                            "63988881111",
                            "carlos.novo@email.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            "Atualizado",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            true,
                            false);

            PessoaDTO.Detalhe detalhe = pessoaService.atualizar(1L, atualizacao);

            assertThat(detalhe.nome()).isEqualTo("Carlos Atualizado");
            assertThat(detalhe.email()).isEqualTo("carlos.novo@email.com");
            assertThat(detalhe.ehBeneficiario()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException se id não existir")
        void atualizarPessoa_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(pessoaRepository.findByIdAndDataInativoIsNull(99L)).thenReturn(Optional.empty());

            var atualizacao =
                    new PessoaDTO.Atualizacao(
                            "Nome",
                            "12345678901",
                            TipoPessoa.FISICA,
                            LocalDate.of(1990, 1, 1),
                            null,
                            "63999998888",
                            "email@email.com",
                            null,
                            null,
                            null,
                            null,
                            List.of(1L),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            false,
                            false);

            assertThatThrownBy(() -> pessoaService.atualizar(99L, atualizacao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Listar Pessoas - Paginação e Filtros")
    class Listar {

        @Test
        @DisplayName("Deve listar pessoas paginadas")
        @SuppressWarnings("unchecked")
        void listarPessoas_semFiltros_retornaPagina() {
            Pageable pageable = PageRequest.of(0, 10);
            Pessoa p = criarPessoaPfTeste();
            p.setId(1L);

            Page<Pessoa> pagina = new PageImpl<>(List.of(p), pageable, 1);
            when(pessoaRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(pagina);

            RespostaPaginada<PessoaDTO.Resumo> resultado =
                    pessoaService.listar(
                            new PessoaFiltroDTO(
                                    null, null, null, null, null, null, null, null, null, null,
                                    null, null, null, null, null, null, false),
                            pageable,
                            false);

            assertThat(resultado.dados()).hasSize(1);
            assertThat(resultado.totalElementos()).isEqualTo(1);
            assertThat(resultado.dados().get(0).nome()).isEqualTo("Carlos");
        }
    }

    @Nested
    @DisplayName("Desativar e Reativar")
    class DesativarReativar {

        @Test
        @DisplayName("Deve desativar pessoa com sucesso")
        void desativarPessoa_comSucesso() {
            Pessoa p = criarPessoaPfTeste();
            p.setId(1L);

            when(pessoaRepository.findByIdAndDataInativoIsNull(1L)).thenReturn(Optional.of(p));

            pessoaService.desativar(1L);

            assertThat(p.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Deve reativar pessoa com sucesso")
        void reativarPessoa_comSucesso() {
            Pessoa p = criarPessoaPfTeste();
            p.setId(1L);
            p.desativar();

            when(pessoaRepository.findById(1L)).thenReturn(Optional.of(p));

            pessoaService.reativar(1L);

            assertThat(p.isAtivo()).isTrue();
        }
    }
}
