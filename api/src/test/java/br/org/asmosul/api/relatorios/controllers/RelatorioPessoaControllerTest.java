package br.org.asmosul.api.relatorios.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.models.Comorbidade;
import br.org.asmosul.api.pessoas.models.Escolaridade;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.models.RendaFamiliar;
import br.org.asmosul.api.pessoas.models.Sexo;
import br.org.asmosul.api.pessoas.models.TipoPessoa;
import br.org.asmosul.api.pessoas.models.Uf;
import br.org.asmosul.api.pessoas.repositories.CategoriaRepository;
import br.org.asmosul.api.pessoas.repositories.ComorbidadeRepository;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("Testes de Integração - RelatorioPessoaController")
class RelatorioPessoaControllerTest extends BaseAPITest {

    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private ComorbidadeRepository comorbidadeRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    private Comorbidade comorbidadeHipertensao;
    private Comorbidade comorbidadeDiabetes;
    private Categoria categoriaIdoso;
    private Categoria categoriaGestante;

    @BeforeEach
    void setUp() {
        pessoaRepository.deleteAll();
        comorbidadeRepository.deleteAll();
        categoriaRepository.deleteAll();

        comorbidadeHipertensao =
                comorbidadeRepository.save(new Comorbidade("Hipertensão", "Pressão arterial alta"));
        comorbidadeDiabetes =
                comorbidadeRepository.save(new Comorbidade("Diabetes", "Diabetes mellitus"));

        categoriaIdoso =
                categoriaRepository.save(new Categoria("Idoso", "Pessoas com 60 anos ou mais"));
        categoriaGestante =
                categoriaRepository.save(
                        new Categoria("Gestante", "Mulheres em período gestacional"));

        Pessoa pessoa1 =
                new Pessoa(
                        "Ana Clara Souza",
                        "11122233344",
                        TipoPessoa.FISICA,
                        LocalDate.of(1960, 5, 10),
                        Sexo.FEMININO,
                        "63991112222",
                        "ana.souza@exemplo.com",
                        Escolaridade.SUPERIOR_COMPLETO,
                        "Aposentada",
                        RendaFamiliar.ENTRE_MIL_E_DOIS_MIL,
                        "Beneficiária ativa",
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Centro",
                        "Rua A",
                        "Casa 1",
                        2,
                        true,
                        false);
        pessoa1.setComorbidades(Set.of(comorbidadeHipertensao, comorbidadeDiabetes));
        pessoa1.setCategorias(Set.of(categoriaIdoso));
        pessoaRepository.save(pessoa1);

        Pessoa pessoa2 =
                new Pessoa(
                        "Bruno Oliveira",
                        "55566677788",
                        TipoPessoa.FISICA,
                        LocalDate.of(1985, 3, 20),
                        Sexo.MASCULINO,
                        "63993334444",
                        "bruno.oliveira@exemplo.com",
                        Escolaridade.MEDIO_COMPLETO,
                        "Comerciante",
                        RendaFamiliar.MAIS_DE_TRES_MIL,
                        "Doador ativo",
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Plano Diretor Sul",
                        "Rua B",
                        "Lote 2",
                        3,
                        false,
                        true);
        pessoa2.setComorbidades(Set.of(comorbidadeHipertensao));
        pessoa2.setCategorias(Set.of(categoriaIdoso, categoriaGestante));
        pessoaRepository.save(pessoa2);

        Pessoa pessoaInativa =
                new Pessoa(
                        "Carlos Inativo",
                        "99988877766",
                        TipoPessoa.FISICA,
                        LocalDate.of(1975, 1, 15),
                        Sexo.MASCULINO,
                        "63995556666",
                        "carlos.inativo@exemplo.com",
                        Escolaridade.FUNDAMENTAL_COMPLETO,
                        "Autônomo",
                        RendaFamiliar.MENOS_DE_MIL,
                        "Registro inativo",
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Centro",
                        "Rua C",
                        null,
                        1,
                        true,
                        false);
        pessoaInativa.desativar();
        pessoaRepository.save(pessoaInativa);
    }

    @Nested
    @DisplayName("GET /relatorios/pessoas/geral")
    class ObterResumoGeralTests {

        @Test
        @DisplayName("Deve retornar 200 OK com indicadores gerais e agrupamento padrão por SEXO")
        void obterResumoGeral_semParametroCriterio_retornaStatus200ComAgrupamentoPorSexo()
                throws Exception {
            mockMvc.perform(
                            get("/relatorios/pessoas/geral")
                                    .with(user("relatorios_user").roles("GERENCIADOR_RELATORIOS")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalBeneficiarios").value(1))
                    .andExpect(jsonPath("$.totalDoadores").value(1))
                    .andExpect(jsonPath("$.totalCadastrosGerais").value(3))
                    .andExpect(jsonPath("$.totalInativos").value(1))
                    .andExpect(jsonPath("$.totalPessoasGrafico").value(2))
                    .andExpect(jsonPath("$.distribuicao.length()").value(2))
                    .andExpect(jsonPath("$.distribuicao[0].quantidade").value(1))
                    .andExpect(jsonPath("$.distribuicao[0].percentual").value(50.0));
        }

        @Test
        @DisplayName(
                "Deve retornar 200 OK com agrupamento por ESCOLARIDADE quando critério informado")
        void obterResumoGeral_comCriterioEscolaridade_retornaStatus200() throws Exception {
            mockMvc.perform(
                            get("/relatorios/pessoas/geral")
                                    .param("criterio", "ESCOLARIDADE")
                                    .with(user("relatorios_user").roles("GERENCIADOR_RELATORIOS")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPessoasGrafico").value(2))
                    .andExpect(jsonPath("$.distribuicao.length()").value(2));
        }

        @Test
        @DisplayName(
                "Deve retornar 200 OK com agrupamento por FAIXA_RENDA quando critério informado")
        void obterResumoGeral_comCriterioFaixaRenda_retornaStatus200() throws Exception {
            mockMvc.perform(
                            get("/relatorios/pessoas/geral")
                                    .param("criterio", "FAIXA_RENDA")
                                    .with(user("relatorios_user").roles("GERENCIADOR_RELATORIOS")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPessoasGrafico").value(2))
                    .andExpect(jsonPath("$.distribuicao.length()").value(2));
        }

        @Test
        @DisplayName("Deve retornar 403 Forbidden quando usuário não possuir perfil de relatórios")
        void obterResumoGeral_semPerfilGerenciadorRelatorios_retornaStatus403() throws Exception {
            mockMvc.perform(get("/relatorios/pessoas/geral")).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /relatorios/pessoas/comorbidades/resumo")
    class ObterResumoComorbidadesTests {

        @Test
        @DisplayName("Deve retornar 200 OK com total de ocorrências, Top 5 e distribuição")
        void obterResumoComorbidades_comDadosValidos_retornaStatus200() throws Exception {
            mockMvc.perform(
                            get("/relatorios/pessoas/comorbidades/resumo")
                                    .with(user("relatorios_user").roles("GERENCIADOR_RELATORIOS")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOcorrencias").value(3))
                    .andExpect(jsonPath("$.top5MaisFrequentes.length()").value(2))
                    .andExpect(jsonPath("$.top5MaisFrequentes[0].posicao").value(1))
                    .andExpect(jsonPath("$.top5MaisFrequentes[0].nome").value("Hipertensão"))
                    .andExpect(jsonPath("$.top5MaisFrequentes[0].quantidade").value(2))
                    .andExpect(jsonPath("$.top5MaisFrequentes[1].posicao").value(2))
                    .andExpect(jsonPath("$.top5MaisFrequentes[1].nome").value("Diabetes"))
                    .andExpect(jsonPath("$.top5MaisFrequentes[1].quantidade").value(1))
                    .andExpect(jsonPath("$.distribuicao.length()").value(2));
        }
    }

    @Nested
    @DisplayName("GET /relatorios/pessoas/comorbidades/tabela")
    class ListarPessoasComorbidadesTests {

        @Test
        @DisplayName("Deve retornar 200 OK com listagem paginada de pessoas com comorbidades")
        void listarPessoasComorbidades_semFiltros_retornaStatus200EListaPaginada()
                throws Exception {
            mockMvc.perform(
                            get("/relatorios/pessoas/comorbidades/tabela")
                                    .param("page", "0")
                                    .param("size", "10")
                                    .param("sort", "nome,asc")
                                    .with(user("relatorios_user").roles("GERENCIADOR_RELATORIOS")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(2))
                    .andExpect(jsonPath("$.dados.length()").value(2))
                    .andExpect(jsonPath("$.dados[0].nome").value("Ana Clara Souza"))
                    .andExpect(jsonPath("$.dados[0].cpf").value("11122233344"))
                    .andExpect(jsonPath("$.dados[0].contato").value("63991112222"))
                    .andExpect(jsonPath("$.dados[0].genero").value("FEMININO"))
                    .andExpect(jsonPath("$.dados[0].comorbidadesDiagnosticadas.length()").value(2));
        }

        @Test
        @DisplayName("Deve retornar 200 OK filtrando por termoBusca e comorbidadeIds")
        void listarPessoasComorbidades_comFiltroTermoBuscaEComorbidadeIds_retornaStatus200()
                throws Exception {
            mockMvc.perform(
                            get("/relatorios/pessoas/comorbidades/tabela")
                                    .param("termoBusca", "Ana")
                                    .param(
                                            "comorbidadeIds",
                                            comorbidadeDiabetes.getId().toString())
                                    .with(user("relatorios_user").roles("GERENCIADOR_RELATORIOS")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nome").value("Ana Clara Souza"));
        }
    }

    @Nested
    @DisplayName("GET /relatorios/pessoas/categorias")
    class ObterResumoCategoriasTests {

        @Test
        @DisplayName(
                "Deve retornar 200 OK com total de vínculos, Top 5 e distribuição de categorias")
        void obterResumoCategorias_comDadosValidos_retornaStatus200() throws Exception {
            mockMvc.perform(
                            get("/relatorios/pessoas/categorias")
                                    .with(user("relatorios_user").roles("GERENCIADOR_RELATORIOS")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalVinculos").value(3))
                    .andExpect(jsonPath("$.top5MaisFrequentes.length()").value(2))
                    .andExpect(jsonPath("$.top5MaisFrequentes[0].posicao").value(1))
                    .andExpect(jsonPath("$.top5MaisFrequentes[0].nome").value("Idoso"))
                    .andExpect(jsonPath("$.top5MaisFrequentes[0].quantidade").value(2))
                    .andExpect(jsonPath("$.top5MaisFrequentes[1].posicao").value(2))
                    .andExpect(jsonPath("$.top5MaisFrequentes[1].nome").value("Gestante"))
                    .andExpect(jsonPath("$.top5MaisFrequentes[1].quantidade").value(1))
                    .andExpect(jsonPath("$.distribuicao.length()").value(2));
        }
    }
}
