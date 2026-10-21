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
import br.org.asmosul.api.doacoes.models.CategoriaItem;
import br.org.asmosul.api.doacoes.models.EnderecoArmazenamento;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import br.org.asmosul.api.doacoes.models.UnidadeMedida;
import br.org.asmosul.api.doacoes.repositories.EnderecoArmazenamentoRepository;
import br.org.asmosul.api.doacoes.repositories.EstoqueRepository;
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
@DisplayName("Testes Unitários - EnderecoArmazenamentoService")
class EnderecoArmazenamentoServiceTest {

    @Mock private EnderecoArmazenamentoRepository enderecoArmazenamentoRepository;
    @Mock private EstoqueRepository estoqueRepository;

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
        @DisplayName("Deve cadastrar endereço com sucesso quando dados válidos")
        void cadastrar_comDadosValidos_cadastraERetornaDetalhe() {
            var requisicao =
                    new EnderecoArmazenamentoDTO.Requisicao(
                            "Depósito Norte",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Norte",
                            "Rua 1",
                            "10",
                            null,
                            "Obs");
            EnderecoArmazenamento salvo = criarEnderecoExemplo(1L, "Depósito Norte");

            when(enderecoArmazenamentoRepository.existsByNomeIgnoreCase("Depósito Norte"))
                    .thenReturn(false);
            when(enderecoArmazenamentoRepository.save(any(EnderecoArmazenamento.class)))
                    .thenReturn(salvo);

            EnderecoArmazenamentoDTO.Detalhe resultado = enderecoArmazenamentoService.cadastrar(requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(1L);
            assertThat(resultado.nome()).isEqualTo("Depósito Norte");
            verify(enderecoArmazenamentoRepository).save(any(EnderecoArmazenamento.class));
        }

        @Test
        @DisplayName("Deve lançar ValidationException de conflito ao cadastrar nome duplicado")
        void cadastrar_comNomeDuplicado_lancaValidationExceptionConflito() {
            var requisicao =
                    new EnderecoArmazenamentoDTO.Requisicao(
                            "Depósito Duplicado",
                            "77000-000",
                            Uf.TO,
                            "Palmas",
                            "Centro",
                            "Av. JK",
                            "10",
                            null,
                            null);

            when(enderecoArmazenamentoRepository.existsByNomeIgnoreCase("Depósito Duplicado"))
                    .thenReturn(true);

            assertThatThrownBy(() -> enderecoArmazenamentoService.cadastrar(requisicao))
                    .isInstanceOf(ConflitoDadosException.class);
        }
    }

    @Nested
    @DisplayName("Listar Endereços de Armazenamento")
    class Listar {

        @Test
        @DisplayName("Deve listar endereços com paginação")
        void listar_comPaginacao_retornaRespostaPaginada() {
            Pageable pageable = PageRequest.of(0, 10);
            EnderecoArmazenamento endereco = criarEnderecoExemplo(1L, "Depósito Central");
            when(enderecoArmazenamentoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(endereco), pageable, 1));

            RespostaPaginada<EnderecoArmazenamentoDTO.Resumo> resultado =
                    enderecoArmazenamentoService.listar(
                            pageable, false, false, null, null, null, null, null, null);

            assertThat(resultado).isNotNull();
            assertThat(resultado.dados()).hasSize(1);
            assertThat(resultado.dados().get(0).nome()).isEqualTo("Depósito Central");
        }

        @Test
        @DisplayName("Deve listar todos os endereços ativos")
        void listarTodas_apenasAtivos_retornaListaResumo() {
            EnderecoArmazenamento e1 = criarEnderecoExemplo(1L, "Dep 1");
            EnderecoArmazenamento e2 = criarEnderecoExemplo(2L, "Dep 2");
            when(enderecoArmazenamentoRepository.findAllByDataInativoIsNull()).thenReturn(List.of(e1, e2));

            List<EnderecoArmazenamentoDTO.Resumo> resultado = enderecoArmazenamentoService.listarTodas(false);

            assertThat(resultado).hasSize(2);
        }

        @Test
        @DisplayName("Deve listar todos incluindo inativos")
        void listarTodas_incluindoInativos_retornaListaResumo() {
            EnderecoArmazenamento e1 = criarEnderecoExemplo(1L, "Dep 1");
            EnderecoArmazenamento e2 = criarEnderecoExemplo(2L, "Dep 2");
            when(enderecoArmazenamentoRepository.findAll()).thenReturn(List.of(e1, e2));

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
            when(enderecoArmazenamentoRepository.findById(5L)).thenReturn(Optional.of(endereco));
            when(estoqueRepository.findAllByArmazenamentoIdAndQuantidadeGreaterThan(5L, 0))
                    .thenReturn(List.of());

            EnderecoArmazenamentoDTO.Detalhe resultado = enderecoArmazenamentoService.buscarPorId(5L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(5L);
            assertThat(resultado.nome()).isEqualTo("Depósito Sul");
            assertThat(resultado.itens()).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar detalhes do endereço incluindo itens em estoque com saldo maior que zero (US-91)")
        void buscarPorId_quandoPossuiItensEmEstoque_retornaListaDeItens() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(5L, "Depósito Sul");
            when(enderecoArmazenamentoRepository.findById(5L)).thenReturn(Optional.of(endereco));

            CategoriaItem categoria = new CategoriaItem("Alimentos", "Desc");
            Item item = new Item("Arroz 5kg", categoria, 2000, UnidadeMedida.PACOTE, "");
            ReflectionTestUtils.setField(item, "id", 10L);

            Estoque estoque = new Estoque(item, endereco, 50);
            when(estoqueRepository.findAllByArmazenamentoIdAndQuantidadeGreaterThan(5L, 0))
                    .thenReturn(List.of(estoque));

            EnderecoArmazenamentoDTO.Detalhe resultado = enderecoArmazenamentoService.buscarPorId(5L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.itens()).hasSize(1);
            assertThat(resultado.itens().get(0).idItem()).isEqualTo(10L);
            assertThat(resultado.itens().get(0).nomeItem()).isEqualTo("Arroz 5kg");
            assertThat(resultado.itens().get(0).categoria()).isEqualTo("Alimentos");
            assertThat(resultado.itens().get(0).unidadeMedida()).isEqualTo(UnidadeMedida.PACOTE);
            assertThat(resultado.itens().get(0).quantidade()).isEqualTo(50);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando não encontrado")
        void buscarPorId_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(enderecoArmazenamentoRepository.findById(99L)).thenReturn(Optional.empty());

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
            when(estoqueRepository.findAllByArmazenamentoIdAndQuantidadeGreaterThan(6L, 0))
                    .thenReturn(List.of());

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
        @DisplayName("Deve lançar ValidationException ao atualizar para nome já existente")
        void atualizar_comNomeDuplicado_lancaValidationException() {
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
                            "Plano Diretor",
                            "Av. Teotônio",
                            "200",
                            "Galpão 2",
                            "Novo depósito");

            assertThatThrownBy(() -> enderecoArmazenamentoService.atualizar(6L, requisicao))
                    .isInstanceOf(ConflitoDadosException.class);
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao atualizar ID inexistente")
        void atualizar_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(99L))
                    .thenReturn(Optional.empty());

            var requisicao =
                    new EnderecoArmazenamentoDTO.Atualizacao(
                            "Nome",
                            "77000-111",
                            Uf.TO,
                            "Palmas",
                            "Plano Diretor",
                            "Av. Teotônio",
                            "200",
                            null,
                            null);

            assertThatThrownBy(() -> enderecoArmazenamentoService.atualizar(99L, requisicao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Desativar e Reativar")
    class DesativarEReativar {

        @Test
        @DisplayName("Deve desativar endereço quando não possuir itens vinculados")
        void desativar_semItensVinculados_desativaComSucesso() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(7L, "Dep Ativo");
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(7L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoArmazenamentoRepository.possuiItensVinculados(7L)).thenReturn(false);

            enderecoArmazenamentoService.desativar(7L);

            assertThat(endereco.isAtivo()).isFalse();
            assertThat(endereco.getDataInativo()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar ValidationException ao tentar desativar endereço com itens vinculados (RN012)")
        void desativar_comItensVinculados_lancaValidationException() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(8L, "Dep Com Itens");
            when(enderecoArmazenamentoRepository.findByIdAndDataInativoIsNull(8L))
                    .thenReturn(Optional.of(endereco));
            when(enderecoArmazenamentoRepository.possuiItensVinculados(8L)).thenReturn(true);

            assertThatThrownBy(() -> enderecoArmazenamentoService.desativar(8L))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Deve reativar endereço com sucesso")
        void reativar_comIdExistente_reativaComSucesso() {
            EnderecoArmazenamento endereco = criarEnderecoExemplo(9L, "Dep Inativo");
            endereco.desativar();
            when(enderecoArmazenamentoRepository.findById(9L)).thenReturn(Optional.of(endereco));

            enderecoArmazenamentoService.reativar(9L);

            assertThat(endereco.isAtivo()).isTrue();
            assertThat(endereco.getDataInativo()).isNull();
        }
    }
}
