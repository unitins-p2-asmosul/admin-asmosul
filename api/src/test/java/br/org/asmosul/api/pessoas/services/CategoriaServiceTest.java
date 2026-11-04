package br.org.asmosul.api.pessoas.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.pessoas.dtos.CategoriaDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - CategoriaService")
class CategoriaServiceTest {

    @Mock private CategoriaRepository categoriaRepository;

    @InjectMocks private CategoriaService categoriaService;

    @Test
    @DisplayName("Deve retornar quantidade de pessoas pertencentes na listagem paginada")
    void listar_comPessoasVinculadas_retornaPessoasPertencentes() {
        Pageable paginacao = PageRequest.of(0, 10);
        Categoria categoria = new Categoria("Associado", "Membro associado");
        ReflectionTestUtils.setField(categoria, "id", 1L);

        when(categoriaRepository.findAllByDataInativoIsNull(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(categoria), paginacao, 1));
        when(categoriaRepository.contarPessoasAtivasPorCategoriaIds(List.of(1L)))
                .thenReturn(List.<Object[]>of(new Object[] {1L, 12L}));

        RespostaPaginada<CategoriaDTO.Resumo> resposta =
                categoriaService.listar(paginacao, false, null);

        assertThat(resposta.dados()).hasSize(1);
        assertThat(resposta.dados().get(0).nome()).isEqualTo("Associado");
        assertThat(resposta.dados().get(0).pessoasPertencentes()).isEqualTo(12);
    }

    @Test
    @DisplayName("Deve retornar quantidade de pessoas pertencentes em listarTodas")
    void listarTodas_comPessoasVinculadas_retornaPessoasPertencentes() {
        Categoria categoria = new Categoria("Idoso", "Pessoa idosa");
        ReflectionTestUtils.setField(categoria, "id", 2L);

        when(categoriaRepository.findAllByDataInativoIsNull()).thenReturn(List.of(categoria));
        when(categoriaRepository.contarPessoasAtivasPorCategoriaIds(List.of(2L)))
                .thenReturn(List.<Object[]>of(new Object[] {2L, 5L}));

        List<CategoriaDTO.Resumo> resultado = categoriaService.listarTodas(false);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).pessoasPertencentes()).isEqualTo(5);
    }
}
