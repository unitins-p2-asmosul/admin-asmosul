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
import br.org.asmosul.api.doacoes.dtos.EnderecoDoacaoDTO;
import br.org.asmosul.api.doacoes.models.EnderecoDoacao;
import br.org.asmosul.api.doacoes.repositories.EnderecoDoacaoRepository;
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
@DisplayName("Testes Unitários - EnderecoDoacaoService")
class EnderecoDoacaoServiceTest {

    @Mock private EnderecoDoacaoRepository enderecoDoacaoRepository;

    @InjectMocks private EnderecoDoacaoService enderecoDoacaoService;

    private EnderecoDoacao criarEnderecoExemplo(Long id, String nome) {
        EnderecoDoacao endereco =
                new EnderecoDoacao(
                        nome,
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Centro",
                        "Av. JK",
                        "100",
                        "Sala 1",
                        "Depósito principal");
        ReflectionTestUtils.setField(endereco, "id", id);
        return endereco;
    }

    @Nested
    @DisplayName("Cadastrar Endereço de Armazenamento")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar endereço com dados válidos")
        void cadastrar_comDadosValidos_salvaERetornaDetalhe() {
            var requisicao =
                    new EnderecoDoacaoDTO.Requisicao(
                            "Galpão Central",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            "Sala 1",
                            "Depósito principal");

            when(enderecoDoacaoRepository.existsByNomeIgnoreCase("Galpão Central")).thenReturn(false);

            EnderecoDoacao salvo = criarEnderecoExemplo(1L, "Galpão Central");
            when(enderecoDoacaoRepository.save(any(EnderecoDoacao.class))).thenReturn(salvo);

            EnderecoDoacaoDTO.Detalhe resultado = enderecoDoacaoService.cadastrar(requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(1L);
            assertThat(resultado.nome()).isEqualTo("Galpão Central");
            assertThat(resultado.ativo()).isTrue();
            verify(enderecoDoacaoRepository).save(any(EnderecoDoacao.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException quando nome já existir (RN03)")
        void cadastrar_comNomeDuplicado_lancaConflitoDadosException() {
            var requisicao =
                    new EnderecoDoacaoDTO.Requisicao(
                            "Galpão Central",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);

            when(enderecoDoacaoRepository.existsByNomeIgnoreCase("Galpão Central")).thenReturn(true);

            assertThatThrownBy(() -> enderecoDoacaoService.cadastrar(requisicao))
                    .isInstanceOf(ConflitoDadosException.class)
                    .hasMessageContaining("Já existe um endereço de armazenamento cadastrado com este nome.");
        }
    }

    @Nested
    @DisplayName("Listar Endereços de Armazenamento (Paginado)")
    class Listar {

        @Test
        @DisplayName("Deve retornar listagem paginada sanitizada com filtros")
        void listar_comPaginacaoESanitizacao_retornaRespostaPaginada() {
            Pageable paginacao = PageRequest.of(0, 10);
            EnderecoDoacao endereco = criarEnderecoExemplo(2L, "Depósito A");

            when(enderecoDoacaoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(endereco), paginacao, 1));

            RespostaPaginada<EnderecoDoacaoDTO.Resumo> resposta =
                    enderecoDoacaoService.listar(
                            paginacao, false, "Depósito", "77000", Uf.TO, "Palmas", "Centro", "Av");

            assertThat(resposta).isNotNull();
            assertThat(resposta.dados()).hasSize(1);
            assertThat(resposta.dados().get(0).nome()).isEqualTo("Depósito A");
            assertThat(resposta.totalElementos()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Listar Todos os Endereços de Armazenamento")
    class ListarTodas {

        @Test
        @DisplayName("Deve listar apenas ativos quando incluirInativos for falso")
        void listarTodas_apenasAtivos_retornaLista() {
            EnderecoDoacao ativo = criarEnderecoExemplo(3L, "Depósito Ativo");
            when(enderecoDoacaoRepository.findAllByDataInativoIsNull()).thenReturn(List.of(ativo));

            List<EnderecoDoacaoDTO.Resumo> resultado = enderecoDoacaoService.listarTodas(false);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).nome()).isEqualTo("Depósito Ativo");
        }

        @Test
        @DisplayName("Deve listar todos incluindo inativos quando incluirInativos for verdadeiro")
        void listarTodas_incluindoInativos_retornaListaCompleta() {
            EnderecoDoacao ativo = criarEnderecoExemplo(3L, "Depósito Ativo");
            EnderecoDoacao inativo = criarEnderecoExemplo(4L, "Depósito Inativo");
            inativo.desativar();

            when(enderecoDoacaoRepository.findAll()).thenReturn(List.of(ativo, inativo));

            List<EnderecoDoacaoDTO.Resumo> resultado = enderecoDoacaoService.listarTodas(true);

            assertThat(resultado).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Buscar Endereço de Armazenamento por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes do endereço quando ativo e encontrado")
        void buscarPorId_comIdExistenteEAtivo_retornaDetalhe() {
            EnderecoDoacao endereco = criarEnderecoExemplo(5L, "Depósito Sul");
            when(enderecoDoacaoRepository.findByIdAndDataInativoIsNull(5L))
                    .thenReturn(Optional.of(endereco));

            EnderecoDoacaoDTO.Detalhe resultado = enderecoDoacaoService.buscarPorId(5L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(5L);
            assertThat(resultado.nome()).isEqualTo("Depósito Sul");
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando não encontrado ou inativo")
        void buscarPorId_comIdInexistenteOuInativo_lancaEntidadeNaoEncontradaException() {
            when(enderecoDoacaoRepository.findByIdAndDataInativoIsNull(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> enderecoDoacaoService.buscarPorId(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Atualizar Endereço de Armazenamento")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar endereço com sucesso")
        void atualizar_comDadosValidos_atualizaERetornaDetalhe() {
            EnderecoDoacao endereco = criarEnderecoExemplo(6L, "Nome Antigo");
            when(enderecoDoacaoRepository.findByIdAndDataInativoIsNull(6L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoDoacaoRepository.existsByNomeIgnoreCaseAndIdNot("Nome Novo", 6L))
                    .thenReturn(false);

            var requisicao =
                    new EnderecoDoacaoDTO.Atualizacao(
                            "Nome Novo",
                            "77000-111",
                            Uf.TO,
                            "Palmas",
                            "Plano Diretor",
                            "Av. Teotônio",
                            "200",
                            "Galpão 2",
                            "Novo depósito");

            EnderecoDoacaoDTO.Detalhe resultado = enderecoDoacaoService.atualizar(6L, requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.nome()).isEqualTo("Nome Novo");
            assertThat(endereco.getNome()).isEqualTo("Nome Novo");
            assertThat(endereco.getNumero()).isEqualTo("200");
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException ao atualizar para nome já existente")
        void atualizar_comNomeDuplicado_lancaConflitoDadosException() {
            EnderecoDoacao endereco = criarEnderecoExemplo(6L, "Nome Antigo");
            when(enderecoDoacaoRepository.findByIdAndDataInativoIsNull(6L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoDoacaoRepository.existsByNomeIgnoreCaseAndIdNot("Nome Existente", 6L))
                    .thenReturn(true);

            var requisicao =
                    new EnderecoDoacaoDTO.Atualizacao(
                            "Nome Existente",
                            "77000-111",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);

            assertThatThrownBy(() -> enderecoDoacaoService.atualizar(6L, requisicao))
                    .isInstanceOf(ConflitoDadosException.class);
        }
    }

    @Nested
    @DisplayName("Desativar Endereço de Armazenamento (RN012)")
    class Desativar {

        @Test
        @DisplayName("Deve desativar endereço ativo quando não houver itens vinculados")
        void desativar_semItensVinculados_inativaComSucesso() {
            EnderecoDoacao endereco = criarEnderecoExemplo(7L, "Depósito Vazio");
            when(enderecoDoacaoRepository.findByIdAndDataInativoIsNull(7L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoDoacaoRepository.possuiItensVinculados(7L)).thenReturn(false);

            enderecoDoacaoService.desativar(7L);

            assertThat(endereco.isAtivo()).isFalse();
            assertThat(endereco.getDataInativo()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar ValidationException (HTTP 400) ao desativar endereço com itens vinculados (RN012)")
        void desativar_comItensVinculados_lancaValidationException() {
            EnderecoDoacao endereco = criarEnderecoExemplo(7L, "Depósito Ocupado");
            when(enderecoDoacaoRepository.findByIdAndDataInativoIsNull(7L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoDoacaoRepository.possuiItensVinculados(7L)).thenReturn(true);

            assertThatThrownBy(() -> enderecoDoacaoService.desativar(7L))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(
                            ex -> {
                                ValidationException ve = (ValidationException) ex;
                                assertThat(ve.getErros())
                                        .anyMatch(
                                                e ->
                                                        e.mensagem()
                                                                .contains(
                                                                        "Não é possível desativar um endereço que possui itens armazenados vinculados"));
                            });
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException se o endereço já estiver inativo ou não existir")
        void desativar_comIdInexistenteOuJaInativo_lancaEntidadeNaoEncontradaException() {
            when(enderecoDoacaoRepository.findByIdAndDataInativoIsNull(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> enderecoDoacaoService.desativar(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Reativar Endereço de Armazenamento")
    class Reativar {

        @Test
        @DisplayName("Deve reativar endereço inativo com sucesso")
        void reativar_comIdExistente_reativaComSucesso() {
            EnderecoDoacao endereco = criarEnderecoExemplo(8L, "Depósito Reativado");
            endereco.desativar();
            when(enderecoDoacaoRepository.findById(8L)).thenReturn(Optional.of(endereco));

            enderecoDoacaoService.reativar(8L);

            assertThat(endereco.isAtivo()).isTrue();
            assertThat(endereco.getDataInativo()).isNull();
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao reativar endereço inexistente")
        void reativar_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(enderecoDoacaoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enderecoDoacaoService.reativar(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }
}
