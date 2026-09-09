package br.org.asmosul.api.pessoas.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("Testes de Integração - PessoaController")
class PessoaControllerTest extends BaseAPITest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ComorbidadeRepository comorbidadeRepository;

    private Categoria categoriaPadrao;
    private Comorbidade comorbidadePadrao;

    @BeforeEach
    void setUp() {
        pessoaRepository.deleteAll();
        comorbidadeRepository.deleteAll();
        categoriaRepository.deleteAll();

        categoriaPadrao =
                categoriaRepository.save(new Categoria("Sócio Efetivo", "Categoria base"));
        comorbidadePadrao =
                comorbidadeRepository.save(new Comorbidade("Hipertensão", "Comorbidade base"));
    }

    private String criarRequisicaoPfValidaJson() {
        return """
                {
                    "nome": "Maria Silva",
                    "cpfCnpj": "12345678901",
                    "tipoPessoa": "FISICA",
                    "dataNascimento": "15-05-1990",
                    "sexo": "FEMININO",
                    "telefone": "63987654321",
                    "email": "maria.silva@exemplo.com",
                    "escolaridade": "SUPERIOR_COMPLETO",
                    "profissao": "Analista de Sistemas",
                    "rendaFamiliar": "MAIS_DE_TRES_MIL",
                    "comorbidades": [%d],
                    "categorias": [%d],
                    "descricao": "Pessoa cadastrada para acompanhamento.",
                    "cep": "77000-000",
                    "uf": "TO",
                    "cidade": "Palmas",
                    "bairro": "Plano Diretor Norte",
                    "logradouro": "Rua 1",
                    "complementoEndereco": "Apto 101",
                    "quantidadeCoabitantes": 2,
                    "ehBeneficiario": true,
                    "ehDoador": false
                }
                """
                .formatted(comorbidadePadrao.getId(), categoriaPadrao.getId());
    }

    private String criarRequisicaoPjValidaJson() {
        return """
                {
                    "nome": "Empresa Solidária LTDA",
                    "cpfCnpj": "12345678000199",
                    "tipoPessoa": "JURIDICA",
                    "telefone": "63988887777",
                    "email": "contato@empresa.com",
                    "categorias": [%d],
                    "descricao": "Doações corporativas",
                    "cep": "77000-000",
                    "uf": "TO",
                    "cidade": "Palmas",
                    "bairro": "Centro",
                    "logradouro": "Avenida JK",
                    "complementoEndereco": "Sala 200",
                    "quantidadeCoabitantes": 0,
                    "ehBeneficiario": false,
                    "ehDoador": true
                }
                """
                .formatted(categoriaPadrao.getId());
    }

    private String criarAtualizacaoPfValidaJson() {
        return """
                {
                    "nome": "Nome Atualizado",
                    "cpfCnpj": "12345678901",
                    "tipoPessoa": "FISICA",
                    "dataNascimento": "01-01-1990",
                    "sexo": "MASCULINO",
                    "telefone": "63988887777",
                    "email": "novo@email.com",
                    "escolaridade": "SUPERIOR_COMPLETO",
                    "profissao": "Gerente",
                    "rendaFamiliar": "MAIS_DE_TRES_MIL",
                    "comorbidades": [%d],
                    "categorias": [%d],
                    "descricao": "Nova descrição",
                    "cep": "77000-000",
                    "uf": "TO",
                    "cidade": "Palmas",
                    "bairro": "Plano Diretor",
                    "logradouro": "Rua 2",
                    "complementoEndereco": "Apto",
                    "quantidadeCoabitantes": 1,
                    "ehBeneficiario": false,
                    "ehDoador": true
                }
                """
                .formatted(comorbidadePadrao.getId(), categoriaPadrao.getId());
    }

    private Pessoa criarPessoaPfSalva(String nome, String cpfCnpj, String email) {
        Pessoa p =
                new Pessoa(
                        nome,
                        cpfCnpj,
                        TipoPessoa.FISICA,
                        LocalDate.of(1990, 1, 1),
                        Sexo.MASCULINO,
                        "63999998888",
                        email,
                        Escolaridade.SUPERIOR_COMPLETO,
                        "Analista",
                        RendaFamiliar.MAIS_DE_TRES_MIL,
                        "Desc",
                        "77000-000",
                        Uf.TO,
                        "Palmas",
                        "Centro",
                        "Rua 1",
                        "Apto",
                        0,
                        true,
                        false);
        p.setCategorias(new java.util.HashSet<>(List.of(categoriaPadrao)));
        p.setComorbidades(new java.util.HashSet<>(List.of(comorbidadePadrao)));
        return pessoaRepository.save(p);
    }

    @Nested
    @DisplayName("POST /pessoas - Cadastro de Pessoa")
    class Cadastrar {

        @Test
        @DisplayName(
                "Deve cadastrar Pessoa Física com sucesso retornando status 201 e Location header")
        void cadastrar_comPessoaFisicaValida_retornaStatus201ELocationHeader() throws Exception {
            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(criarRequisicaoPfValidaJson()))
                    .andExpect(status().isCreated())
                    .andExpect(
                            header().string(
                                            "Location",
                                            org.hamcrest.Matchers.matchesPattern(
                                                    ".*/pessoas/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("Maria Silva"))
                    .andExpect(jsonPath("$.cpfCnpj").value("12345678901"))
                    .andExpect(jsonPath("$.tipoPessoa.codigo").value("FISICA"))
                    .andExpect(jsonPath("$.ehBeneficiario").value(true));
        }

        @Test
        @DisplayName("Deve cadastrar Pessoa Jurídica com sucesso retornando status 201")
        void cadastrar_comPessoaJuridicaValida_retornaStatus201ELocationHeader() throws Exception {
            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(criarRequisicaoPjValidaJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nome").value("Empresa Solidária LTDA"))
                    .andExpect(jsonPath("$.cpfCnpj").value("12345678000199"))
                    .andExpect(jsonPath("$.tipoPessoa.codigo").value("JURIDICA"))
                    .andExpect(jsonPath("$.ehBeneficiario").value(false))
                    .andExpect(jsonPath("$.ehDoador").value(true));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome estiver em branco")
        void cadastrar_comNomeEmBranco_retornaStatus400() throws Exception {
            var jsonPayload =
                    """
                    {
                        "nome": "",
                        "cpfCnpj": "12345678901",
                        "telefone": "63987654321"
                    }
                    """;

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o CPF/CNPJ for inválido")
        void cadastrar_comCpfCnpjInvalido_retornaStatus400() throws Exception {
            var jsonPayload =
                    """
                    {
                        "nome": "Maria Silva",
                        "cpfCnpj": "12345",
                        "telefone": "63987654321"
                    }
                    """;

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando PJ for cadastrada como beneficiária (RN05)")
        void cadastrar_comPessoaJuridicaEBeneficiarioTrue_retornaStatus400() throws Exception {
            var jsonPayload =
                    """
                    {
                        "nome": "Empresa Solidária",
                        "cpfCnpj": "12345678000199",
                        "tipoPessoa": "JURIDICA",
                        "telefone": "63988887777",
                        "email": "pj@empresa.com",
                        "categorias": [%d],
                        "descricao": "Desc",
                        "cep": "77000-000",
                        "uf": "TO",
                        "cidade": "Palmas",
                        "bairro": "Centro",
                        "logradouro": "Avenida JK",
                        "complementoEndereco": "Sala 200",
                        "quantidadeCoabitantes": 0,
                        "ehBeneficiario": true,
                        "ehDoador": true
                    }
                    """
                            .formatted(categoriaPadrao.getId());

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de Validação"));
        }

        @Test
        @DisplayName("Deve retornar status 409 quando CPF/CNPJ já existir (RN03)")
        void cadastrar_comCpfCnpjDuplicado_retornaStatus409() throws Exception {
            criarPessoaPfSalva("Pessoa Existente", "12345678901", "existente@email.com");

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(criarRequisicaoPfValidaJson()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }

        @Test
        @DisplayName("Deve retornar status 409 quando E-mail já existir (RN03)")
        void cadastrar_comEmailDuplicado_retornaStatus409() throws Exception {
            criarPessoaPfSalva("Pessoa Existente", "98765432100", "maria.silva@exemplo.com");

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(criarRequisicaoPfValidaJson()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"));
        }
    }

    @Nested
    @DisplayName("GET /pessoas - Listagem Paginada")
    class Listar {

        @Test
        @DisplayName("Deve listar pessoas paginadas com sucesso retornando status 200")
        void listar_comPaginacaoEFiltros_retornaStatus200() throws Exception {
            criarPessoaPfSalva("Ana Clara", "11122233344", "ana@email.com");
            criarPessoaPfSalva("Bruno Silva", "55566677788", "bruno@email.com");

            mockMvc.perform(
                            get("/pessoas")
                                    .param("page", "0")
                                    .param("size", "10")
                                    .param("nome", "Ana"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados").isArray())
                    .andExpect(jsonPath("$.dados[0].nome").value("Ana Clara"))
                    .andExpect(jsonPath("$.totalElementos").value(1));
        }
    }

    @Nested
    @DisplayName("GET /pessoas/todas - Listagem Não Paginada")
    class ListarTodas {

        @Test
        @DisplayName("Deve listar todas as pessoas ativas retornando status 200")
        void listarTodas_retornaStatus200() throws Exception {
            criarPessoaPfSalva("Carlos", "11122233344", "carlos@email.com");

            mockMvc.perform(get("/pessoas/todas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].nome").value("Carlos"));
        }
    }

    @Nested
    @DisplayName("GET /pessoas/{id} - Detalhamento")
    class BuscarPorId {

        @Test
        @DisplayName("Deve buscar pessoa por ID com sucesso retornando status 200")
        void buscarPorId_comIdExistente_retornaStatus200() throws Exception {
            Pessoa p = criarPessoaPfSalva("Maria", "12345678901", "maria@email.com");

            mockMvc.perform(get("/pessoas/{id}", p.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(p.getId()))
                    .andExpect(jsonPath("$.nome").value("Maria"))
                    .andExpect(jsonPath("$.categorias").isArray())
                    .andExpect(jsonPath("$.comorbidades").isArray());
        }

        @Test
        @DisplayName("Deve retornar status 404 para ID inexistente")
        void buscarPorId_comIdInexistenteOuInativo_retornaStatus404() throws Exception {
            mockMvc.perform(get("/pessoas/{id}", 999999L)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /pessoas/{id} - Atualização")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar pessoa com sucesso retornando status 200")
        void atualizar_comDadosValidos_retornaStatus200() throws Exception {
            Pessoa p = criarPessoaPfSalva("Nome Antigo", "12345678901", "antigo@email.com");

            mockMvc.perform(
                            put("/pessoas/{id}", p.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(criarAtualizacaoPfValidaJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(p.getId()))
                    .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
                    .andExpect(jsonPath("$.email").value("novo@email.com"));
        }

        @Test
        @DisplayName("Deve retornar status 400 ao tentar alterar o tipo de pessoa (RN08)")
        void atualizar_tentandoAlterarTipoPessoa_retornaStatus400() throws Exception {
            Pessoa p = criarPessoaPfSalva("Pessoa Fisica", "12345678901", "pf@email.com");

            var jsonPayload =
                    """
                    {
                        "nome": "Pessoa Fisica",
                        "cpfCnpj": "12345678000199",
                        "tipoPessoa": "JURIDICA",
                        "telefone": "63988887777",
                        "email": "pf@email.com",
                        "categorias": [%d],
                        "descricao": "Desc",
                        "cep": "77000-000",
                        "uf": "TO",
                        "cidade": "Palmas",
                        "bairro": "Centro",
                        "logradouro": "Rua 1",
                        "quantidadeCoabitantes": 0,
                        "ehBeneficiario": false,
                        "ehDoador": true
                    }
                    """
                            .formatted(categoriaPadrao.getId());

            mockMvc.perform(
                            put("/pessoas/{id}", p.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de Validação"));
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar pessoa inexistente")
        void atualizar_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(
                            put("/pessoas/{id}", 999999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(criarAtualizacaoPfValidaJson()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /pessoas/{id}/desativar - Desativação")
    class Desativar {

        @Test
        @DisplayName("Deve desativar pessoa com sucesso retornando status 204")
        void desativar_comIdExistente_retornaStatus204() throws Exception {
            Pessoa p = criarPessoaPfSalva("Ativo", "12345678901", "ativo@email.com");

            mockMvc.perform(patch("/pessoas/{id}/desativar", p.getId()))
                    .andExpect(status().isNoContent());

            Pessoa inativo = pessoaRepository.findById(p.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertNotNull(inativo.getDataInativo());
        }
    }

    @Nested
    @DisplayName("PATCH /pessoas/{id}/reativar - Reativação")
    class Reativar {

        @Test
        @DisplayName("Deve reativar pessoa com sucesso retornando status 204")
        void reativar_comIdExistente_retornaStatus204() throws Exception {
            Pessoa p = criarPessoaPfSalva("Inativo", "12345678901", "inativo@email.com");
            p.desativar();
            pessoaRepository.save(p);

            mockMvc.perform(patch("/pessoas/{id}/reativar", p.getId()))
                    .andExpect(status().isNoContent());

            Pessoa reativado = pessoaRepository.findById(p.getId()).orElseThrow();
            org.junit.jupiter.api.Assertions.assertNull(reativado.getDataInativo());
        }
    }

    @Nested
    @DisplayName("DELETE /pessoas/{id} - Exclusão Física")
    class Excluir {

        @Test
        @DisplayName("Deve excluir pessoa fisicamente com sucesso retornando status 204")
        void excluir_comIdExistente_retornaStatus204() throws Exception {
            Pessoa p = criarPessoaPfSalva("Para Excluir", "12345678901", "excluir@email.com");

            mockMvc.perform(delete("/pessoas/{id}", p.getId())).andExpect(status().isNoContent());

            org.junit.jupiter.api.Assertions.assertFalse(
                    pessoaRepository.findById(p.getId()).isPresent());
        }
    }
}
