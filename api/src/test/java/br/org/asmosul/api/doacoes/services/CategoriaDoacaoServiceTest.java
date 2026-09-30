package br.org.asmosul.api.doacoes.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.doacoes.dtos.CategoriaDoacaoDTO;
import br.org.asmosul.api.doacoes.models.CategoriaDoacao;
import br.org.asmosul.api.doacoes.repositories.CategoriaDoacaoRepository;
import java.time.LocalDateTime;
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
@DisplayName("Testes Unitários - CategoriaDoacaoService")
class CategoriaDoacaoServiceTest {

    @Mock private CategoriaDoacaoRepository categoriaDoacaoRepository;

    @InjectMocks private CategoriaDoacaoService categoriaDoacaoService;

    @Nested
    @DisplayName("Cadastrar Categoria de Doação")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar categoria de doação com sucesso")
        void cadastrar_comDadosValidos_salvaERetornaDetalhe() {
            var requisicao = new CategoriaDoacaoDTO.Requisicao("Alimentos", "Cestas e mantimentos");
            when(categoriaDoacaoRepository.existsByNomeIgnoreCase("Alimentos")).thenReturn(false);

            CategoriaDoacao salva = new CategoriaDoacao("Alimentos", "Cestas e mantimentos");
            ReflectionTestUtils.setField(salva, "id", 1L);
            when(categoriaDoacaoRepository.save(any(CategoriaDoacao.class))).thenReturn(salva);

            CategoriaDoacaoDTO.Detalhe resultado = categoriaDoacaoService.cadastrar(requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(1L);
            assertThat(resultado.nome()).isEqualTo("Alimentos");
            assertThat(resultado.descricao()).isEqualTo("Cestas e mantimentos");
            assertThat(resultado.ativo()).isTrue();
            assertThat(resultado.dataInativo()).isNull();
            verify(categoriaDoacaoRepository).save(any(CategoriaDoacao.class));
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException quando nome já existir (RN03)")
        void cadastrar_comNomeDuplicado_lancaConflitoDadosException() {
            var requisicao = new CategoriaDoacaoDTO.Requisicao("Alimentos", "Desc");
            when(categoriaDoacaoRepository.existsByNomeIgnoreCase("Alimentos")).thenReturn(true);

            assertThatThrownBy(() -> categoriaDoacaoService.cadastrar(requisicao))
                    .isInstanceOf(ConflitoDadosException.class)
                    .hasMessageContaining("Já existe uma categoria de doação cadastrada com este nome.");
        }
    }

    @Nested
    @DisplayName("Listar Categorias de Doação (Paginado)")
    class Listar {

        @Test
        @DisplayName("Deve retornar listagem paginada sanitizada com sucesso")
        void listar_comPaginacaoESanitizacao_retornaRespostaPaginada() {
            Pageable paginacao = PageRequest.of(0, 10);
            CategoriaDoacao cat = new CategoriaDoacao("Roupas", "Vestuário geral");
            ReflectionTestUtils.setField(cat, "id", 2L);

            when(categoriaDoacaoRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(cat), paginacao, 1));

            RespostaPaginada<CategoriaDoacaoDTO.Resumo> resposta =
                    categoriaDoacaoService.listar(paginacao, false, "Roupas", null);

            assertThat(resposta).isNotNull();
            assertThat(resposta.dados()).hasSize(1);
            assertThat(resposta.dados().get(0).nome()).isEqualTo("Roupas");
            assertThat(resposta.totalElementos()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Listar Todas as Categorias de Doação")
    class ListarTodas {

        @Test
        @DisplayName("Deve listar apenas ativas quando incluirInativos for falso")
        void listarTodas_apenasAtivas_retornaLista() {
            CategoriaDoacao cat = new CategoriaDoacao("Higiene", "Sabonetes e pastas");
            ReflectionTestUtils.setField(cat, "id", 3L);
            when(categoriaDoacaoRepository.findAllByDataInativoIsNull()).thenReturn(List.of(cat));

            List<CategoriaDoacaoDTO.Resumo> resultado = categoriaDoacaoService.listarTodas(false);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).nome()).isEqualTo("Higiene");
        }

        @Test
        @DisplayName("Deve listar todas incluindo inativos quando incluirInativos for verdadeiro")
        void listarTodas_incluindoInativos_retornaListaCompleta() {
            CategoriaDoacao ativa = new CategoriaDoacao("Higiene", "Desc");
            ReflectionTestUtils.setField(ativa, "id", 3L);
            CategoriaDoacao inativa = new CategoriaDoacao("Brinquedos", "Desc");
            ReflectionTestUtils.setField(inativa, "id", 4L);
            inativa.desativar();

            when(categoriaDoacaoRepository.findAll()).thenReturn(List.of(ativa, inativa));

            List<CategoriaDoacaoDTO.Resumo> resultado = categoriaDoacaoService.listarTodas(true);

            assertThat(resultado).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Buscar Categoria de Doação por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes da categoria quando ativa e encontrada")
        void buscarPorId_comIdExistenteEAtivo_retornaDetalhe() {
            CategoriaDoacao cat = new CategoriaDoacao("Móveis", "Mesas e cadeiras");
            ReflectionTestUtils.setField(cat, "id", 5L);
            when(categoriaDoacaoRepository.findByIdAndDataInativoIsNull(5L))
                    .thenReturn(Optional.of(cat));

            CategoriaDoacaoDTO.Detalhe resultado = categoriaDoacaoService.buscarPorId(5L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(5L);
            assertThat(resultado.nome()).isEqualTo("Móveis");
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException quando não encontrada ou inativa")
        void buscarPorId_comIdInexistenteOuInativo_lancaEntidadeNaoEncontradaException() {
            when(categoriaDoacaoRepository.findByIdAndDataInativoIsNull(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoriaDoacaoService.buscarPorId(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class)
                    .hasMessageContaining("Categoria de doação ativa não encontrada");
        }
    }

    @Nested
    @DisplayName("Atualizar Categoria de Doação")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar dados da categoria com sucesso")
        void atualizar_comDadosValidos_atualizaERetornaDetalhe() {
            CategoriaDoacao cat = new CategoriaDoacao("Nome Antigo", "Desc Antiga");
            ReflectionTestUtils.setField(cat, "id", 6L);
            when(categoriaDoacaoRepository.findByIdAndDataInativoIsNull(6L))
                    .thenReturn(Optional.of(cat));
            when(categoriaDoacaoRepository.existsByNomeIgnoreCaseAndIdNot("Nome Novo", 6L))
                    .thenReturn(false);

            var requisicao = new CategoriaDoacaoDTO.Atualizacao("Nome Novo", "Desc Nova");
            CategoriaDoacaoDTO.Detalhe resultado = categoriaDoacaoService.atualizar(6L, requisicao);

            assertThat(resultado).isNotNull();
            assertThat(resultado.nome()).isEqualTo("Nome Novo");
            assertThat(resultado.descricao()).isEqualTo("Desc Nova");
            assertThat(cat.getNome()).isEqualTo("Nome Novo");
            assertThat(cat.getDescricao()).isEqualTo("Desc Nova");
        }

        @Test
        @DisplayName("Deve lançar ConflitoDadosException ao atualizar para nome já existente")
        void atualizar_comNomeDuplicado_lancaConflitoDadosException() {
            CategoriaDoacao cat = new CategoriaDoacao("Nome Antigo", "Desc");
            ReflectionTestUtils.setField(cat, "id", 6L);
            when(categoriaDoacaoRepository.findByIdAndDataInativoIsNull(6L))
                    .thenReturn(Optional.of(cat));
            when(categoriaDoacaoRepository.existsByNomeIgnoreCaseAndIdNot("Nome Existente", 6L))
                    .thenReturn(true);

            var requisicao = new CategoriaDoacaoDTO.Atualizacao("Nome Existente", "Desc");

            assertThatThrownBy(() -> categoriaDoacaoService.atualizar(6L, requisicao))
                    .isInstanceOf(ConflitoDadosException.class)
                    .hasMessageContaining("Já existe uma categoria de doação cadastrada com este nome.");
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao atualizar registro inexistente ou inativo")
        void atualizar_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(categoriaDoacaoRepository.findByIdAndDataInativoIsNull(99L))
                    .thenReturn(Optional.empty());

            var requisicao = new CategoriaDoacaoDTO.Atualizacao("Nome", "Desc");

            assertThatThrownBy(() -> categoriaDoacaoService.atualizar(99L, requisicao))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Desativar Categoria de Doação (Soft Delete)")
    class Desativar {

        @Test
        @DisplayName("Deve inativar categoria ativa com sucesso")
        void desativar_comIdExistenteEAtivo_inativaComSucesso() {
            CategoriaDoacao cat = new CategoriaDoacao("Eletros", "Geladeiras e fogões");
            ReflectionTestUtils.setField(cat, "id", 7L);
            when(categoriaDoacaoRepository.findByIdAndDataInativoIsNull(7L))
                    .thenReturn(Optional.of(cat));

            categoriaDoacaoService.desativar(7L);

            assertThat(cat.isAtivo()).isFalse();
            assertThat(cat.getDataInativo()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao tentar desativar categoria já inativa ou inexistente")
        void desativar_comIdInexistenteOuJaInativo_lancaEntidadeNaoEncontradaException() {
            when(categoriaDoacaoRepository.findByIdAndDataInativoIsNull(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoriaDoacaoService.desativar(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("Reativar Categoria de Doação")
    class Reativar {

        @Test
        @DisplayName("Deve reativar categoria previamente inativa com sucesso")
        void reativar_comIdExistente_reativaComSucesso() {
            CategoriaDoacao cat = new CategoriaDoacao("Material Escolar", "Cadernos");
            ReflectionTestUtils.setField(cat, "id", 8L);
            cat.desativar();
            when(categoriaDoacaoRepository.findById(8L)).thenReturn(Optional.of(cat));

            categoriaDoacaoService.reativar(8L);

            assertThat(cat.isAtivo()).isTrue();
            assertThat(cat.getDataInativo()).isNull();
        }

        @Test
        @DisplayName("Deve lançar EntidadeNaoEncontradaException ao reativar categoria inexistente")
        void reativar_comIdInexistente_lancaEntidadeNaoEncontradaException() {
            when(categoriaDoacaoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoriaDoacaoService.reativar(99L))
                    .isInstanceOf(EntidadeNaoEncontradaException.class);
        }
    }
}
