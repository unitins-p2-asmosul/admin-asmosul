package br.org.asmosul.api.pessoas.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.comum.dtos.RespostaPaginada;
import br.org.asmosul.api.comum.exceptions.ConflitoDadosException;
import br.org.asmosul.api.comum.exceptions.EntidadeNaoEncontradaException;
import br.org.asmosul.api.pessoas.dtos.PessoaDTO;
import br.org.asmosul.api.pessoas.models.Escolaridade;
import br.org.asmosul.api.pessoas.models.RendaFamiliar;
import br.org.asmosul.api.pessoas.models.Sexo;
import br.org.asmosul.api.pessoas.services.PessoaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PessoaController.class)
@DisplayName("Testes Unitários - PessoaController")
class PessoaControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private PessoaService pessoaService;

    private String criarRequisicaoValidaJson() {
        return """
                {
                    "nome": "Maria Silva",
                    "cpf": "12345678901",
                    "dataNascimento": "15-05-1990",
                    "sexo": "F",
                    "telefone": "63987654321",
                    "email": "maria.silva@exemplo.com",
                    "escolaridade": "SUPERIOR_COMPLETO",
                    "profissao": "Analista de Sistemas",
                    "rendaFamiliar": "MAIS_DE_TRES",
                    "comorbidades": [1],
                    "categorias": [2],
                    "descricao": "Observação sobre a associada"
                }
                """;
    }

    private String criarAtualizacaoValidaJson() {
        return """
                {
                    "nome": "Maria Silva Atualizada",
                    "cpf": "12345678901",
                    "dataNascimento": "15-05-1990",
                    "sexo": "F",
                    "telefone": "63987654321",
                    "email": "maria.atualizada@exemplo.com",
                    "escolaridade": "SUPERIOR_COMPLETO",
                    "profissao": "Gerente de Projetos",
                    "rendaFamiliar": "MAIS_DE_TRES",
                    "comorbidades": [1],
                    "categorias": [2],
                    "descricao": "Nova descrição"
                }
                """;
    }

    private PessoaDTO.Detalhe criarDetalheValido() {
        return new PessoaDTO.Detalhe(
                1L,
                "Maria Silva",
                "12345678901",
                LocalDate.of(1990, 5, 15),
                Sexo.F,
                "63987654321",
                "maria.silva@exemplo.com",
                Escolaridade.SUPERIOR_COMPLETO,
                "Analista de Sistemas",
                RendaFamiliar.MAIS_DE_TRES_MIL,
                List.of(1L),
                List.of(2L),
                "Observação sobre a associada",
                true);
    }

    @Nested
    @DisplayName("POST /pessoas - Cadastro de Pessoa")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar pessoa com sucesso retornando status 201 e Location header")
        void deveCadastrarPessoaComSucesso() throws Exception {
            var jsonPayload = criarRequisicaoValidaJson();
            var detalheRetornado = criarDetalheValido();

            when(pessoaService.cadastrar(any(PessoaDTO.Requisicao.class)))
                    .thenReturn(detalheRetornado);

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", endsWith("/pessoas/1")))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Maria Silva"))
                    .andExpect(jsonPath("$.cpf").value("12345678901"))
                    .andExpect(jsonPath("$.dataNascimento").value("15-05-1990"))
                    .andExpect(jsonPath("$.sexo.codigo").value("F"))
                    .andExpect(jsonPath("$.telefone").value("63987654321"))
                    .andExpect(jsonPath("$.email").value("maria.silva@exemplo.com"))
                    .andExpect(jsonPath("$.escolaridade.codigo").value("SUPERIOR_COMPLETO"))
                    .andExpect(jsonPath("$.rendaFamiliar.codigo").value("MAIS_DE_TRES"))
                    .andExpect(jsonPath("$.comorbidades[0]").value(1L))
                    .andExpect(jsonPath("$.categorias[0]").value(2L))
                    .andExpect(jsonPath("$.ativo").value(true));

            verify(pessoaService).cadastrar(any(PessoaDTO.Requisicao.class));
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o nome estiver em branco")
        void deveRetornar400AoCadastrarComNomeEmBranco() throws Exception {
            var requisicao =
                    new PessoaDTO.Requisicao(
                            "",
                            "12345678901",
                            LocalDate.of(1990, 5, 15),
                            Sexo.F,
                            "63987654321",
                            "maria@exemplo.com",
                            Escolaridade.SUPERIOR_COMPLETO,
                            "Profissão",
                            RendaFamiliar.MAIS_DE_TRES_MIL,
                            List.of(),
                            List.of(),
                            null);

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o CPF for inválido (não contiver 11 dígitos)")
        void deveRetornar400AoCadastrarComCpfInvalido() throws Exception {
            var requisicao =
                    new PessoaDTO.Requisicao(
                            "Maria Silva",
                            "12345", // CPF inválido
                            LocalDate.of(1990, 5, 15),
                            Sexo.F,
                            "63987654321",
                            "maria@exemplo.com",
                            Escolaridade.SUPERIOR_COMPLETO,
                            "Profissão",
                            RendaFamiliar.MAIS_DE_TRES_MIL,
                            List.of(),
                            List.of(),
                            null);

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o telefone for inválido")
        void deveRetornar400AoCadastrarComTelefoneInvalido() throws Exception {
            var requisicao =
                    new PessoaDTO.Requisicao(
                            "Maria Silva",
                            "12345678901",
                            LocalDate.of(1990, 5, 15),
                            Sexo.F,
                            "123", // Telefone inválido (< 10 dígitos)
                            "maria@exemplo.com",
                            Escolaridade.SUPERIOR_COMPLETO,
                            "Profissão",
                            RendaFamiliar.MAIS_DE_TRES_MIL,
                            List.of(),
                            List.of(),
                            null);

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando o e-mail for mal formatado")
        void deveRetornar400AoCadastrarComEmailInvalido() throws Exception {
            var requisicao =
                    new PessoaDTO.Requisicao(
                            "Maria Silva",
                            "12345678901",
                            LocalDate.of(1990, 5, 15),
                            Sexo.F,
                            "63987654321",
                            "email-invalido", // Formato de e-mail inválido
                            Escolaridade.SUPERIOR_COMPLETO,
                            "Profissão",
                            RendaFamiliar.MAIS_DE_TRES_MIL,
                            List.of(),
                            List.of(),
                            null);

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando a data de nascimento for nula")
        void deveRetornar400AoCadastrarComDataNascimentoNula() throws Exception {
            var requisicao =
                    new PessoaDTO.Requisicao(
                            "Maria Silva",
                            "12345678901",
                            null,
                            Sexo.F,
                            "63987654321",
                            "maria@exemplo.com",
                            Escolaridade.SUPERIOR_COMPLETO,
                            "Profissão",
                            RendaFamiliar.MAIS_DE_TRES_MIL,
                            List.of(),
                            List.of(),
                            null);

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName(
                "Deve retornar status 409 quando o serviço lançar ConflitoDadosException para CPF/Email duplicado")
        void deveRetornar409AoCadastrarComCpfDuplicado() throws Exception {
            var jsonPayload = criarRequisicaoValidaJson();

            when(pessoaService.cadastrar(any(PessoaDTO.Requisicao.class)))
                    .thenThrow(
                            new ConflitoDadosException(
                                    "Já existe uma pessoa cadastrada com este CPF."));

            mockMvc.perform(
                            post("/pessoas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Conflito de Dados"))
                    .andExpect(
                            jsonPath("$.detail")
                                    .value("Já existe uma pessoa cadastrada com este CPF."));
        }
    }

    @Nested
    @DisplayName("GET /pessoas - Listagem Paginada")
    class ListarPaginado {

        @Test
        @DisplayName("Deve retornar listagem paginada de pessoas com status 200")
        void deveListarPessoasPaginadas() throws Exception {
            var resumo =
                    new PessoaDTO.Resumo(
                            1L,
                            "Maria Silva",
                            "12345678901",
                            "63987654321",
                            "maria@exemplo.com",
                            true);
            var respostaPaginada = new RespostaPaginada<>(List.of(resumo), 0, 10, 1L, 1);

            when(pessoaService.listar(any(Pageable.class), eq(false))).thenReturn(respostaPaginada);

            mockMvc.perform(get("/pessoas").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].id").value(1L))
                    .andExpect(jsonPath("$.dados[0].nome").value("Maria Silva"))
                    .andExpect(jsonPath("$.dados[0].cpf").value("12345678901"))
                    .andExpect(jsonPath("$.dados[0].telefone").value("63987654321"))
                    .andExpect(jsonPath("$.dados[0].email").value("maria@exemplo.com"))
                    .andExpect(jsonPath("$.dados[0].ativo").value(true))
                    .andExpect(jsonPath("$.paginaAtual").value(0))
                    .andExpect(jsonPath("$.tamanhoPagina").value(10))
                    .andExpect(jsonPath("$.totalElementos").value(1L))
                    .andExpect(jsonPath("$.totalPaginas").value(1));

            verify(pessoaService).listar(any(Pageable.class), eq(false));
        }

        @Test
        @DisplayName("Deve repassar o parâmetro incluirInativos para o serviço")
        void deveListarPessoasComParametroIncluirInativos() throws Exception {
            var respostaPaginada = new RespostaPaginada<PessoaDTO.Resumo>(List.of(), 0, 10, 0L, 0);

            when(pessoaService.listar(any(Pageable.class), eq(true))).thenReturn(respostaPaginada);

            mockMvc.perform(get("/pessoas").param("incluirInativos", "true"))
                    .andExpect(status().isOk());

            verify(pessoaService).listar(any(Pageable.class), eq(true));
        }
    }

    @Nested
    @DisplayName("GET /pessoas/todas - Listagem Completa Não Paginada")
    class ListarTodas {

        @Test
        @DisplayName("Deve retornar todas as pessoas em lista simples com status 200")
        void deveListarTodasAsPessoas() throws Exception {
            var lista =
                    List.of(
                            new PessoaDTO.Resumo(
                                    1L,
                                    "Pessoa 1",
                                    "11111111111",
                                    "63987654321",
                                    "p1@exemplo.com",
                                    true),
                            new PessoaDTO.Resumo(
                                    2L,
                                    "Pessoa 2",
                                    "22222222222",
                                    "63987654322",
                                    "p2@exemplo.com",
                                    true));

            when(pessoaService.listarTodas(false)).thenReturn(lista);

            mockMvc.perform(get("/pessoas/todas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].nome").value("Pessoa 1"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].nome").value("Pessoa 2"));

            verify(pessoaService).listarTodas(false);
        }

        @Test
        @DisplayName("Deve repassar incluirInativos ao buscar todas as pessoas")
        void deveListarTodasComParametroIncluirInativos() throws Exception {
            when(pessoaService.listarTodas(true)).thenReturn(List.of());

            mockMvc.perform(get("/pessoas/todas").param("incluirInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(pessoaService).listarTodas(true);
        }
    }

    @Nested
    @DisplayName("GET /pessoas/{id} - Busca por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes da pessoa quando encontrada com status 200")
        void deveBuscarPessoaPorIdComSucesso() throws Exception {
            var detalhe = criarDetalheValido();

            when(pessoaService.buscarPorId(1L)).thenReturn(detalhe);

            mockMvc.perform(get("/pessoas/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Maria Silva"))
                    .andExpect(jsonPath("$.cpf").value("12345678901"))
                    .andExpect(jsonPath("$.dataNascimento").value("15-05-1990"))
                    .andExpect(jsonPath("$.comorbidades[0]").value(1L))
                    .andExpect(jsonPath("$.categorias[0]").value(2L))
                    .andExpect(jsonPath("$.ativo").value(true));

            verify(pessoaService).buscarPorId(1L);
        }

        @Test
        @DisplayName(
                "Deve retornar status 404 quando o serviço lançar EntidadeNaoEncontradaException")
        void deveRetornar404AoBuscarPessoaInexistente() throws Exception {
            when(pessoaService.buscarPorId(99L))
                    .thenThrow(
                            new EntidadeNaoEncontradaException(
                                    "Pessoa ativa não encontrada com o ID informado: 99"));

            mockMvc.perform(get("/pessoas/{id}", 99L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Recurso Não Encontrado"))
                    .andExpect(
                            jsonPath("$.detail")
                                    .value("Pessoa ativa não encontrada com o ID informado: 99"));
        }
    }

    @Nested
    @DisplayName("PUT /pessoas/{id} - Atualização")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar pessoa com sucesso retornando status 200")
        void deveAtualizarPessoaComSucesso() throws Exception {
            var jsonPayload = criarAtualizacaoValidaJson();
            var detalhe =
                    new PessoaDTO.Detalhe(
                            1L,
                            "Maria Silva Atualizada",
                            "12345678901",
                            LocalDate.of(1990, 5, 15),
                            Sexo.F,
                            "63987654321",
                            "maria.atualizada@exemplo.com",
                            Escolaridade.SUPERIOR_COMPLETO,
                            "Gerente de Projetos",
                            RendaFamiliar.MAIS_DE_TRES_MIL,
                            List.of(1L),
                            List.of(2L),
                            "Nova descrição",
                            true);

            when(pessoaService.atualizar(eq(1L), any(PessoaDTO.Atualizacao.class)))
                    .thenReturn(detalhe);

            mockMvc.perform(
                            put("/pessoas/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.nome").value("Maria Silva Atualizada"))
                    .andExpect(jsonPath("$.profissao").value("Gerente de Projetos"));

            verify(pessoaService).atualizar(eq(1L), any(PessoaDTO.Atualizacao.class));
        }

        @Test
        @DisplayName("Deve retornar status 400 ao atualizar com campos inválidos")
        void deveRetornar400AoAtualizarComCamposInvalidos() throws Exception {
            var requisicao =
                    new PessoaDTO.Atualizacao(
                            "", // nome vazio
                            "123", // cpf invalido
                            null, // data nula
                            Sexo.F,
                            "123", // telefone invalido
                            "invalido", // email invalido
                            Escolaridade.SUPERIOR_COMPLETO,
                            "Profissao",
                            RendaFamiliar.MAIS_DE_TRES_MIL,
                            List.of(),
                            List.of(),
                            null);

            mockMvc.perform(
                            put("/pessoas/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requisicao)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar atualizar pessoa inexistente")
        void deveRetornar404AoAtualizarPessoaInexistente() throws Exception {
            var jsonPayload = criarAtualizacaoValidaJson();

            when(pessoaService.atualizar(eq(99L), any(PessoaDTO.Atualizacao.class)))
                    .thenThrow(
                            new EntidadeNaoEncontradaException(
                                    "Pessoa ativa não encontrada com o ID informado: 99"));

            mockMvc.perform(
                            put("/pessoas/{id}", 99L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName(
                "Deve retornar status 409 ao atualizar para CPF/Email já existente de outra pessoa")
        void deveRetornar409AoAtualizarParaCpfDuplicado() throws Exception {
            var jsonPayload = criarAtualizacaoValidaJson();

            when(pessoaService.atualizar(eq(1L), any(PessoaDTO.Atualizacao.class)))
                    .thenThrow(
                            new ConflitoDadosException(
                                    "Já existe uma pessoa cadastrada com este CPF."));

            mockMvc.perform(
                            put("/pessoas/{id}", 1L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(jsonPayload))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PATCH /pessoas/{id}/desativar - Desativação (Soft Delete)")
    class Desativar {

        @Test
        @DisplayName("Deve desativar pessoa com sucesso retornando status 204")
        void deveDesativarPessoaComSucesso() throws Exception {
            doNothing().when(pessoaService).desativar(1L);

            mockMvc.perform(patch("/pessoas/{id}/desativar", 1L)).andExpect(status().isNoContent());

            verify(pessoaService).desativar(1L);
        }

        @Test
        @DisplayName(
                "Deve retornar status 404 ao tentar desativar pessoa inexistente ou já inativa")
        void deveRetornar404AoDesativarPessoaInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Pessoa ativa não encontrada com o ID informado: 99"))
                    .when(pessoaService)
                    .desativar(99L);

            mockMvc.perform(patch("/pessoas/{id}/desativar", 99L)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /pessoas/{id}/reativar - Reativação")
    class Reativar {

        @Test
        @DisplayName("Deve reativar pessoa com sucesso retornando status 204")
        void deveReativarPessoaComSucesso() throws Exception {
            doNothing().when(pessoaService).reativar(1L);

            mockMvc.perform(patch("/pessoas/{id}/reativar", 1L)).andExpect(status().isNoContent());

            verify(pessoaService).reativar(1L);
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar reativar pessoa inexistente")
        void deveRetornar404AoReativarPessoaInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Pessoa não encontrada com o ID informado: 99"))
                    .when(pessoaService)
                    .reativar(99L);

            mockMvc.perform(patch("/pessoas/{id}/reativar", 99L)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /pessoas/{id} - Exclusão Física")
    class Excluir {

        @Test
        @DisplayName("Deve excluir pessoa com sucesso retornando status 204")
        void deveExcluirPessoaComSucesso() throws Exception {
            doNothing().when(pessoaService).excluir(1L);

            mockMvc.perform(delete("/pessoas/{id}", 1L)).andExpect(status().isNoContent());

            verify(pessoaService).excluir(1L);
        }

        @Test
        @DisplayName("Deve retornar status 404 ao tentar excluir pessoa inexistente")
        void deveRetornar404AoExcluirPessoaInexistente() throws Exception {
            doThrow(
                            new EntidadeNaoEncontradaException(
                                    "Pessoa não encontrada com o ID informado: 99"))
                    .when(pessoaService)
                    .excluir(99L);

            mockMvc.perform(delete("/pessoas/{id}", 99L)).andExpect(status().isNotFound());
        }
    }
}
