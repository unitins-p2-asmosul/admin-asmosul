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
import br.org.asmosul.api.doacoes.dtos.EnderecoArmazenamentoDTO;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
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
class EnderecoArmazenamentoServiceTest {

    @Mock private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;

    @InjectMocks private EnderecoArmazenamentoService enderecoArmazenamentoService;

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
                    new EnderecoArmazenamentoDTO.Requisicao(
                            "Galpão Central",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            "Sala 1",
                            "Depósito principal");

            when(enderecoArmazenamentoRepository.existsByNomeIgnoreCase("Galpão Central")).thenReturn(false);

            EnderecoArmazenamento salvo = criarEnderecoExemplo(1L, "Galpão Central");
            when(enderecoArmazenamentoRepository.save(any(EnderecoArmazenamento.class))).thenReturn(salvo);

            EnderecoArmazenamentoDTO.Detalhe resultado = enderecoArmazenamentoService.cadastrar(requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(1L);
            assertThat(resultado.nome()).isEqualTo("Galpão Central");
            assertThat(resultado.ativo()).isTrue();
            verify(enderecoArmazenamentoRepository).save(any(EnderecoArmazenamento.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException quando nome já existir (RN03)")
        void cadastrar_comNomeDuplicado_lancaConflitoDadosException() {
            var requisicao =
                    new EnderecoArmazenamentoDTO.Requisicao(
                            "Galpão Central",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);

            when(enderecoArmazenamentoRepository.existsByNomeIgnoreCase("Galpão Central")).thenReturn(true);

            assertThatThrownBy(() -> enderecoArmazenamentoService.cadastrar(requisicao))
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
            EnderecoArmazenamento endereco = criarEnderecoExemplo(2L, "Depósito A");

            when(enderecoArmazenamentoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(endereco), paginacao, 1));

            RespostaPaginada<EnderecoArmazenamentoDTO.Resumo> resposta =
                    enderecoArmazenamentoService.listar(
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
            EnderecoArmazenamento ativo = criarEnderecoExemplo(3L, "Depósito Ativo");
            when(enderecoArmazenamentoRepository.findAllByDataInativoIsNull()).thenReturn(List.of(ativo));

            List<EnderecoArmazenamentoDTO.Resumo> resultado = enderecoArmazenamentoService.listarTodas(false);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).nome()).isEqualTo("Depósito Ativo");
        }

        @Test
        @DisplayName("Deve listar todos incluindo inativos quando incluirInativos for verdadeiro")
        void listarTodas_incluindoInativos_retornaListaCompleta() {
            EnderecoArmazenamento ativo = criarEnderecoExemplo(3L, "Depósito Ativo");
            EnderecoArmazenamento inativo = criarEnderecoExemplo(4L, "Depósito Inativo");
            inativo.desativar();

            when(enderecoArmazenamentoRepository.findAll()).thenReturn(List.of(ativo, inativo));

            List<EnderecoArmazenamentoDTO.Resumo> resultado = enderecoArmazenamentoService.listarTodas(true);

            assertThat(resultado).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Buscar Endereço de Armazenamento por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes do endereço quando ativo e encontrado")
        void buscarPorId_comIdExistenteEAtivo_retornaDetalhe() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(5L, "Depósito Sul");
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(5L))
                    .thenReturn(Optional.of(endereco));

            EnderecoArmazenamentoDTO.Detalhe resultado = enderecoArmazenamentoService.buscarPorId(5L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(5L);
            assertThat(resultado.nome()).isEqualTo("Depósito Sul");
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando não encontrado ou inativo")
        void buscarPorId_comIdInexistenteOuInativo_lancaEntidadeNaoEncontradaException() {
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> enderecoArmazenamentoService.buscarPorId(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Atualizar Endereço de Armazenamento")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar endereço com sucesso")
        void atualizar_comDadosValidos_atualizaERetornaDetalhe() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(6L, "Nome Antigo");
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(6L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoArmazenamentoRepository.existsByNomeIgnoreCaseAndIdNot("Nome Novo", 6L))
                    .thenReturn(false);

            var requisicao =
                    new EnderecoArmazenamentoDTO.Atualizacao(
                            "Nome Novo",
                            "77000-111",
                            Uf.TO,
                            "Palmas",
                            "Plano Diretor",
                            "Av. Teotônio",
                            "200",
                            "Galpão 2",
                            "Novo depósito");

            EnderecoArmazenamentoDTO.Detalhe resultado = enderecoArmazenamentoService.atualizar(6L, requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.nome()).isEqualTo("Nome Novo");
            assertThat(endereco.getNome()).isEqualTo("Nome Novo");
            assertThat(endereco.getNumero()).isEqualTo("200");
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException ao atualizar para nome já existente")
        void atualizar_comNomeDuplicado_lancaConflitoDadosException() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(6L, "Nome Antigo");
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(6L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoArmazenamentoRepository.existsByNomeIgnoreCaseAndIdNot("Nome Existente", 6L))
                    .thenReturn(true);

            var requisicao =
                    new EnderecoArmazenamentoDTO.Atualizacao(
                            "Nome Existente",
                            "77000-111",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "100",
                            null,
                            null);

            assertThatThrownBy(() -> enderecoArmazenamentoService.atualizar(6L, requisicao))
                    .isInstanceOf(ConflitoDadosException.class);
        }
    }

    @Nested
    @DisplayName("Desativar Endereço de Armazenamento (RN012)")
    class Desativar {

        @Test
        @DisplayName("Deve desativar endereço ativo quando não houver itens vinculados")
        void desativar_semItensVinculados_inativaComSucesso() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(7L, "Depósito Vazio");
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(7L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoArmazenamentoRepository.possuiItensVinculados(7L)).thenReturn(false);

            enderecoArmazenamentoService.desativar(7L);

            assertThat(endereco.isAtivo()).isFalse();
            assertThat(endereco.getDataInativo()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar ValidationException (HTTP 400) ao desativar endereço com itens vinculados (RN012)")
        void desativar_comItensVinculados_lancaValidationException() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(7L, "Depósito Ocupado");
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(7L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoArmazenamentoRepository.possuiItensVinculados(7L)).thenReturn(true);

            assertThatThrownBy(() -> enderecoArmazenamentoService.desativar(7L))
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
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> enderecoArmazenamentoService.desativar(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Reativar Endereço de Armazenamento")
    class Reativar {

        @Test
        @DisplayName("Deve reativar endereço inativo com sucesso")
        void reativar_comIdExistente_reativaComSucesso() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(8L, "Depósito Reativado");
            endereco.desativar();
            when(enderecoArmazenamentoRepository.findById(8L)).thenReturn(Optional.of(endereco));

            enderecoArmazenamentoService.reativar(8L);

            assertThat(endereco.isAtivo()).isTrue();
            assertThat(endereco.getDataInativo()).isNull();
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao reativar endereço inexistente")
        void reativar_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(enderecoArmazenamentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> enderecoArmazenamentoService.reativar(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }
}
