package br.org.asmosul.api.pessoas.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.pessoas.dtos.ComorbidadeDTO;
import br.org.asmosul.api.pessoas.models.Comorbidade;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
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
@DisplayName("Testes Unitários - ComorbidadeService")
class ComorbidadeServiceTest {

    @Mock private ComorbidadeRepository comorbidadeRepository;

    @InjectMocks private ComorbidadeService comorbidadeService;

    @Test
    @DisplayName("Deve retornar quantidade de pessoas afetadas na listagem paginada")
    void listar_comPessoasVinculadas_retornaPessoasAfetadas() {
        Pageable paginacao = PageRequest.of(0, 10);
        Comorbidade comorbidade = new Comorbidade("Diabetes", "Tipo 2");
        ReflectionTestUtils.setField(comorbidade, "id", 10L);

        when(comorbidadeRepository.findAllByDataInativoIsNull(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comorbidade), paginacao, 1));
        when(comorbidadeRepository.contarPessoasAtivasPorComorbidadeIds(List.of(10L)))
                .thenReturn(List.<Object[]>of(new Object[] {10L, 8L}));

        RespostaPaginada<ComorbidadeDTO.Resumo> resposta =
                comorbidadeService.listar(paginacao, false, null);

        assertThat(resposta.dados()).hasSize(1);
        assertThat(resposta.dados().get(0).nome()).isEqualTo("Diabetes");
        assertThat(resposta.dados().get(0).pessoasAfetadas()).isEqualTo(8);
    }

    @Test
    @DisplayName("Deve retornar quantidade de pessoas afetadas em listarTodas")
    void listarTodas_comPessoasVinculadas_retornaPessoasAfetadas() {
        Comorbidade comorbidade = new Comorbidade("Asma", "Respiratória");
        ReflectionTestUtils.setField(comorbidade, "id", 11L);

        when(comorbidadeRepository.findAllByDataInativoIsNull()).thenReturn(List.of(comorbidade));
        when(comorbidadeRepository.contarPessoasAtivasPorComorbidadeIds(List.of(11L)))
                .thenReturn(List.<Object[]>of(new Object[] {11L, 3L}));

        List<ComorbidadeDTO.Resumo> resultado = comorbidadeService.listarTodas(false);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).pessoasAfetadas()).isEqualTo(3);
    }
}
