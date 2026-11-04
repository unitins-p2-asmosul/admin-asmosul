package br.org.asmosul.api.relatorios.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.relatorios.dtos.RelatorioPessoaDTO;
import br.org.asmosul.api.relatorios.enums.CriterioAgrupamentoPessoa;
import br.org.asmosul.api.relatorios.repositories.RelatorioPessoaRepository;
import br.org.asmosul.api.relatorios.repositories.projections.AgrupamentoContagemProjection;
import br.org.asmosul.api.relatorios.repositories.projections.PessoaComorbidadeProjection;
import br.org.asmosul.api.relatorios.repositories.projections.TotaisPessoasProjection;
import java.util.List;
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
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - RelatorioPessoaService")
class RelatorioPessoaServiceTest {

    @Mock private RelatorioPessoaRepository relatorioPessoaRepository;

    @InjectMocks private RelatorioPessoaService relatorioPessoaService;

    private TotaisPessoasProjection criarTotaisProjection(
            Long totalAtivo,
            Long totalBeneficiario,
            Long totalDoador,
            Long totalInativo,
            Long totalCadastrosGerais) {
        return new TotaisPessoasProjection() {
            @Override
            public Long getTotalAtivo() {
                return totalAtivo;
            }

            @Override
            public Long getTotalBeneficiario() {
                return totalBeneficiario;
            }

            @Override
            public Long getTotalDoador() {
                return totalDoador;
            }

            @Override
            public Long getTotalInativo() {
                return totalInativo;
            }

            @Override
            public Long getTotalCadastrosGerais() {
                return totalCadastrosGerais;
            }
        };
    }

    private AgrupamentoContagemProjection criarAgrupamentoProjection(String chave, Long total) {
        return new AgrupamentoContagemProjection() {
            @Override
            public String getChave() {
                return chave;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }

    private PessoaComorbidadeProjection criarPessoaComorbidadeProjection(
            Long idPessoa,
            String nome,
            String cpfCnpj,
            String telefone,
            String sexo,
            String comorbidadesConcatenadas) {
        return new PessoaComorbidadeProjection() {
            @Override
            public Long getIdPessoa() {
                return idPessoa;
            }

            @Override
            public String getNome() {
                return nome;
            }

            @Override
            public String getCpfCnpj() {
                return cpfCnpj;
            }

            @Override
            public String getTelefone() {
                return telefone;
            }

            @Override
            public String getSexo() {
                return sexo;
            }

            @Override
            public String getComorbidadesConcatenadas() {
                return comorbidadesConcatenadas;
            }
        };
    }

    @Nested
    @DisplayName("Resumo Geral Demográfico (obterResumoGeral)")
    class ObterResumoGeralTests {

        @Test
        @DisplayName("Deve retornar indicadores e distribuição por SEXO quando critério informado")
        void obterResumoGeral_comCriterioSexo_retornaIndicadoresEDistribuicao() {
            when(relatorioPessoaRepository.buscarTotaisCards())
                    .thenReturn(criarTotaisProjection(8L, 5L, 3L, 2L, 10L));
            when(relatorioPessoaRepository.agruparPorSexo())
                    .thenReturn(
                            List.of(
                                    criarAgrupamentoProjection("FEMININO", 6L),
                                    criarAgrupamentoProjection("MASCULINO", 2L)));

            RelatorioPessoaDTO.ResumoGeral resultado =
                    relatorioPessoaService.obterResumoGeral(CriterioAgrupamentoPessoa.SEXO);

            assertThat(resultado.totalBeneficiarios()).isEqualTo(5L);
            assertThat(resultado.totalDoadores()).isEqualTo(3L);
            assertThat(resultado.totalCadastrosGerais()).isEqualTo(10L);
            assertThat(resultado.totalInativos()).isEqualTo(2L);
            assertThat(resultado.totalPessoasGrafico()).isEqualTo(8L);
            assertThat(resultado.distribuicao()).hasSize(2);
            assertThat(resultado.distribuicao().get(0).rotulo()).isEqualTo("FEMININO");
            assertThat(resultado.distribuicao().get(0).quantidade()).isEqualTo(6L);
            assertThat(resultado.distribuicao().get(0).percentual()).isEqualTo(75.0);
            assertThat(resultado.distribuicao().get(1).rotulo()).isEqualTo("MASCULINO");
            assertThat(resultado.distribuicao().get(1).quantidade()).isEqualTo(2L);
            assertThat(resultado.distribuicao().get(1).percentual()).isEqualTo(25.0);
            verify(relatorioPessoaRepository).agruparPorSexo();
        }

        @Test
        @DisplayName("Deve agrupar por ESCOLARIDADE quando critério ESCOLARIDADE for informado")
        void obterResumoGeral_comCriterioEscolaridade_retornaDistribuicaoEscolaridade() {
            when(relatorioPessoaRepository.buscarTotaisCards())
                    .thenReturn(criarTotaisProjection(4L, 2L, 2L, 0L, 4L));
            when(relatorioPessoaRepository.agruparPorEscolaridade())
                    .thenReturn(
                            List.of(
                                    criarAgrupamentoProjection("SUPERIOR_COMPLETO", 3L),
                                    criarAgrupamentoProjection("MEDIO_COMPLETO", 1L)));

            RelatorioPessoaDTO.ResumoGeral resultado =
                    relatorioPessoaService.obterResumoGeral(CriterioAgrupamentoPessoa.ESCOLARIDADE);

            assertThat(resultado.totalPessoasGrafico()).isEqualTo(4L);
            assertThat(resultado.distribuicao()).hasSize(2);
            assertThat(resultado.distribuicao().get(0).rotulo()).isEqualTo("SUPERIOR_COMPLETO");
            assertThat(resultado.distribuicao().get(0).percentual()).isEqualTo(75.0);
            verify(relatorioPessoaRepository).agruparPorEscolaridade();
        }

        @Test
        @DisplayName("Deve agrupar por FAIXA_RENDA quando critério FAIXA_RENDA for informado")
        void obterResumoGeral_comCriterioFaixaRenda_retornaDistribuicaoFaixaRenda() {
            when(relatorioPessoaRepository.buscarTotaisCards())
                    .thenReturn(criarTotaisProjection(2L, 2L, 0L, 0L, 2L));
            when(relatorioPessoaRepository.agruparPorFaixaRenda())
                    .thenReturn(List.of(criarAgrupamentoProjection("MENOS_DE_MIL", 2L)));

            RelatorioPessoaDTO.ResumoGeral resultado =
                    relatorioPessoaService.obterResumoGeral(CriterioAgrupamentoPessoa.FAIXA_RENDA);

            assertThat(resultado.totalPessoasGrafico()).isEqualTo(2L);
            assertThat(resultado.distribuicao()).hasSize(1);
            assertThat(resultado.distribuicao().get(0).rotulo()).isEqualTo("MENOS_DE_MIL");
            assertThat(resultado.distribuicao().get(0).percentual()).isEqualTo(100.0);
            verify(relatorioPessoaRepository).agruparPorFaixaRenda();
        }
    }

    @Nested
    @DisplayName("Resumo de Comorbidades (obterResumoComorbidades)")
    class ObterResumoComorbidadesTests {

        @Test
        @DisplayName("Deve retornar total de ocorrências, Top 5 e distribuição de comorbidades")
        void obterResumoComorbidades_comDadosExistentes_retornaTop5EDistribuicao() {
            List<AgrupamentoContagemProjection> projecoes =
                    List.of(
                            criarAgrupamentoProjection("Hipertensão", 10L),
                            criarAgrupamentoProjection("Diabetes", 8L),
                            criarAgrupamentoProjection("Asma", 6L),
                            criarAgrupamentoProjection("Cardiopatia", 4L),
                            criarAgrupamentoProjection("Artrose", 2L),
                            criarAgrupamentoProjection("Enxaqueca", 0L));

            when(relatorioPessoaRepository.buscarDistribuicaoComorbidades()).thenReturn(projecoes);

            RelatorioPessoaDTO.ResumoComorbidades resultado =
                    relatorioPessoaService.obterResumoComorbidades();

            assertThat(resultado.totalOcorrencias()).isEqualTo(30L);
            assertThat(resultado.top5MaisFrequentes()).hasSize(5);
            assertThat(resultado.top5MaisFrequentes().get(0).posicao()).isEqualTo(1);
            assertThat(resultado.top5MaisFrequentes().get(0).nome()).isEqualTo("Hipertensão");
            assertThat(resultado.top5MaisFrequentes().get(0).quantidade()).isEqualTo(10L);
            assertThat(resultado.top5MaisFrequentes().get(4).posicao()).isEqualTo(5);
            assertThat(resultado.top5MaisFrequentes().get(4).nome()).isEqualTo("Artrose");
            assertThat(resultado.distribuicao()).hasSize(6);
            assertThat(resultado.distribuicao().get(0).percentual()).isEqualTo(33.33);
        }
    }

    @Nested
    @DisplayName("Tabela de Pessoas com Comorbidades (listarPessoasComorbidades)")
    class ListarPessoasComorbidadesTests {

        @Test
        @DisplayName(
                "Deve retornar página mapeada com lista de comorbidades separadas por vírgula")
        void listarPessoasComorbidades_comFiltrosValidos_retornaRespostaPaginada() {
            PessoaComorbidadeProjection proj =
                    criarPessoaComorbidadeProjection(
                            1L,
                            "Maria Silva",
                            "12345678901",
                            "63987654321",
                            "FEMININO",
                            "Diabetes, Hipertensão");

            when(relatorioPessoaRepository.listarPessoasComorbidades(
                            eq("Maria"), eq(List.of(1L, 2L)), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(proj), PageRequest.of(0, 10), 1));

            RespostaPaginada<RelatorioPessoaDTO.PessoaComorbidadeItem> resposta =
                    relatorioPessoaService.listarPessoasComorbidades(
                            "  Maria  ",
                            List.of(1L, 2L),
                            PageRequest.of(0, 10, Sort.by("nome").ascending()));

            assertThat(resposta.totalElementos()).isEqualTo(1L);
            assertThat(resposta.dados()).hasSize(1);
            RelatorioPessoaDTO.PessoaComorbidadeItem item = resposta.dados().get(0);
            assertThat(item.idPessoa()).isEqualTo(1L);
            assertThat(item.nome()).isEqualTo("Maria Silva");
            assertThat(item.cpf()).isEqualTo("12345678901");
            assertThat(item.contato()).isEqualTo("63987654321");
            assertThat(item.genero()).isEqualTo("FEMININO");
            assertThat(item.comorbidadesDiagnosticadas())
                    .containsExactly("Diabetes", "Hipertensão");
        }

        @Test
        @DisplayName("Deve normalizar termoBusca em branco e lista vazia para null")
        void listarPessoasComorbidades_comFiltrosVazios_passaNullParaRepositorio() {
            when(relatorioPessoaRepository.listarPessoasComorbidades(
                            isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            RespostaPaginada<RelatorioPessoaDTO.PessoaComorbidadeItem> resposta =
                    relatorioPessoaService.listarPessoasComorbidades(
                            "   ", List.of(), PageRequest.of(0, 10));

            assertThat(resposta.dados()).isEmpty();
            verify(relatorioPessoaRepository)
                    .listarPessoasComorbidades(isNull(), isNull(), eq(PageRequest.of(0, 10)));
        }
    }

    @Nested
    @DisplayName("Resumo de Categorias de Pessoas (obterResumoCategorias)")
    class ObterResumoCategoriasTests {

        @Test
        @DisplayName("Deve retornar total de vínculos, Top 5 e distribuição de categorias")
        void obterResumoCategorias_comDadosExistentes_retornaTop5EDistribuicao() {
            List<AgrupamentoContagemProjection> projecoes =
                    List.of(
                            criarAgrupamentoProjection("Idoso", 15L),
                            criarAgrupamentoProjection("Gestante", 5L));

            when(relatorioPessoaRepository.buscarDistribuicaoCategoriasPessoa())
                    .thenReturn(projecoes);

            RelatorioPessoaDTO.ResumoCategoriasPessoa resultado =
                    relatorioPessoaService.obterResumoCategorias();

            assertThat(resultado.totalVinculos()).isEqualTo(20L);
            assertThat(resultado.top5MaisFrequentes()).hasSize(2);
            assertThat(resultado.top5MaisFrequentes().get(0).posicao()).isEqualTo(1);
            assertThat(resultado.top5MaisFrequentes().get(0).nome()).isEqualTo("Idoso");
            assertThat(resultado.top5MaisFrequentes().get(0).quantidade()).isEqualTo(15L);
            assertThat(resultado.distribuicao()).hasSize(2);
            assertThat(resultado.distribuicao().get(0).percentual()).isEqualTo(75.0);
            assertThat(resultado.distribuicao().get(1).percentual()).isEqualTo(25.0);
        }
    }
}
