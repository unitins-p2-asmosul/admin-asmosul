package br.org.asmosul.api.acesso.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.acesso.models.Conta;
import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.models.TipoPessoa;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@DisplayName("Testes de Integração - ContaController")
class ContaControllerTest extends BaseAPITest {

    private static final String SENHA = "Senha@123";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired private ContaRepository contaRepository;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private PasswordEncoder passwordEncoder;


    private Pessoa pessoaFisica;

    @BeforeEach
    void setUp() {
        contaRepository.deleteAll();
        pessoaRepository.deleteAll();

        pessoaFisica = criarPessoa("Maria Silva", "11122233344", TipoPessoa.FISICA, "maria@teste.com");
    }

    private Pessoa criarPessoa(String nome, String cpfCnpj, TipoPessoa tipo, String email) {
        Pessoa pessoa =
                new Pessoa(
                        nome,
                        cpfCnpj,
                        tipo,
                        tipo == TipoPessoa.FISICA ? LocalDate.of(1990, 1, 1) : null,
                        null,
                        "63999999999",
                        email,
                        null,
                        null,
                        null,
                        "Pessoa criada para teste de contas",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        false,
                        false);
        return pessoaRepository.saveAndFlush(pessoa);
    }

    private Conta criarConta(Pessoa pessoa, String nomeUsuario, Set<Perfil> perfis) {
        return contaRepository.saveAndFlush(
                new Conta(pessoa, nomeUsuario, passwordEncoder.encode(SENHA), perfis));
    }

    private Conta criarContaInativa(Pessoa pessoa, String nomeUsuario) {
        Conta conta = new Conta(pessoa, nomeUsuario, passwordEncoder.encode(SENHA), Set.of(Perfil.GERENCIADOR_PESSOAS));
        conta.desativar();
        return contaRepository.saveAndFlush(conta);
    }

    private String requisicaoCadastro(Long pessoaId, String nomeUsuario, String senha) {
        return """
                {
                  "pessoaId": %d,
                  "nomeUsuario": "%s",
                  "senhaTemporaria": "%s",
                  "perfis": ["GERENCIADOR_ACESSO", "GERENCIADOR_PESSOAS"]
                }
                """
                .formatted(pessoaId, nomeUsuario, senha);
    }

    private String hoje() {
        return LocalDate.now().format(FORMATO_DATA);
    }

    private String ontem() {
        return LocalDate.now().minusDays(1).format(FORMATO_DATA);
    }

    @Nested
    @DisplayName("POST /acessos/contas - Cadastro de Conta")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar conta com senha temporária, redefinirSenha = true e Location")
        void cadastrar_comDadosValidos_retornaStatus201() throws Exception {
            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requisicaoCadastro(pessoaFisica.getId(), "maria.silva", SENHA)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.pessoaId").value(pessoaFisica.getId()))
                    .andExpect(jsonPath("$.nomePessoa").value("Maria Silva"))
                    .andExpect(jsonPath("$.email").value("maria@teste.com"))
                    .andExpect(jsonPath("$.nomeUsuario").value("maria.silva"))
                    .andExpect(jsonPath("$.redefinirSenha").value(true))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andExpect(jsonPath("$.dataCriacao").value(hoje()))
                    .andExpect(
                            jsonPath("$.perfis[*].codigo")
                                    .value(containsInAnyOrder("GERENCIADOR_ACESSO", "GERENCIADOR_PESSOAS")));

            Conta conta = contaRepository.findByNomeUsuario("maria.silva").orElseThrow();
            assertThat(conta.isRedefinirSenha()).isTrue();
            assertThat(conta.getSenhaHash()).isNotEqualTo(SENHA);
            assertThat(passwordEncoder.matches(SENHA, conta.getSenhaHash())).isTrue();
        }

        @Test
        @DisplayName("Deve retornar 400 ao cadastrar conta para pessoa jurídica (RN018)")
        void cadastrar_comPessoaJuridica_retornaStatus400() throws Exception {
            Pessoa empresa =
                    criarPessoa("Empresa LTDA", "12345678000199", TipoPessoa.JURIDICA, "empresa@teste.com");

            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requisicaoCadastro(empresa.getId(), "empresa", SENHA)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.invalidFields.pessoaId")
                                    .value("Apenas pessoas físicas podem possuir contas."));
        }

        @Test
        @DisplayName("Deve retornar 400 ao cadastrar conta para pessoa inativa")
        void cadastrar_comPessoaInativa_retornaStatus400() throws Exception {
            pessoaFisica.desativar();
            pessoaRepository.saveAndFlush(pessoaFisica);

            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requisicaoCadastro(pessoaFisica.getId(), "maria.silva", SENHA)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.invalidFields.pessoaId")
                                    .value("A pessoa informada está inativa."));
        }

        @Test
        @DisplayName("Deve cadastrar conta para pessoa sem e-mail (RN09 revogada)")
        void cadastrar_comPessoaSemEmail_retornaStatus201() throws Exception {
            Pessoa semEmail = criarPessoa("João Sem Email", "55566677788", TipoPessoa.FISICA, null);

            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requisicaoCadastro(semEmail.getId(), "joao", SENHA)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").doesNotExist());
        }

        @Test
        @DisplayName("Deve retornar 400 quando campos obrigatórios estiverem em branco ou vazios")
        void cadastrar_comCamposEmBranco_retornaStatus400() throws Exception {
            String corpo =
                    """
                    {
                      "pessoaId": null,
                      "nomeUsuario": "",
                      "senhaTemporaria": "",
                      "perfis": []
                    }
                    """;

            mockMvc.perform(post("/acessos/contas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando a lista de perfis contiver valor vazio")
        void cadastrar_comPerfilVazioNaLista_retornaStatus400() throws Exception {
            String corpo =
                    """
                    {
                      "pessoaId": %d,
                      "nomeUsuario": "maria.silva",
                      "senhaTemporaria": "Senha@123",
                      "perfis": [""]
                    }
                    """
                            .formatted(pessoaFisica.getId());

            mockMvc.perform(post("/acessos/contas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando o perfil informado não existir")
        void cadastrar_comPerfilInexistente_retornaStatus400() throws Exception {
            String corpo =
                    """
                    {
                      "pessoaId": %d,
                      "nomeUsuario": "maria.silva",
                      "senhaTemporaria": "Senha@123",
                      "perfis": ["PERFIL_INEXISTENTE"]
                    }
                    """
                            .formatted(pessoaFisica.getId());

            mockMvc.perform(post("/acessos/contas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando a senha temporária exceder 72 bytes")
        void cadastrar_comSenhaAcimaDe72Bytes_retornaStatus400() throws Exception {
            // 37 caracteres acentuados = 74 bytes em UTF-8
            String senhaLonga = "é".repeat(37);

            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requisicaoCadastro(pessoaFisica.getId(), "maria.silva", senhaLonga)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.invalidFields.senhaTemporaria")
                                    .value("A senha deve ter no máximo 72 bytes."));

            assertThat(contaRepository.existsByNomeUsuario("maria.silva")).isFalse();
        }

        @Test
        @DisplayName("Deve aceitar senha temporária com exatamente 72 bytes")
        void cadastrar_comSenhaDe72Bytes_retornaStatus201() throws Exception {
            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            requisicaoCadastro(
                                                    pessoaFisica.getId(), "maria.silva", "a".repeat(72))))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve retornar 404 quando a pessoa informada não existir")
        void cadastrar_comPessoaInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requisicaoCadastro(999999L, "fantasma", SENHA)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 409 quando o nome de usuário já estiver em uso")
        void cadastrar_comNomeUsuarioDuplicado_retornaStatus409() throws Exception {
            Pessoa outra = criarPessoa("Outra Pessoa", "99988877766", TipoPessoa.FISICA, "outra@teste.com");
            criarConta(outra, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requisicaoCadastro(pessoaFisica.getId(), "maria.silva", SENHA)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve retornar 409 quando a pessoa já possuir conta (RN010)")
        void cadastrar_comPessoaQueJaPossuiConta_retornaStatus409() throws Exception {
            criarConta(pessoaFisica, "maria.antiga", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            post("/acessos/contas")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requisicaoCadastro(pessoaFisica.getId(), "maria.nova", SENHA)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PUT /acessos/contas/{id} - Alteração de nome de usuário")
    class Atualizar {

        @Test
        @DisplayName("Deve alterar apenas o nome de usuário, mantendo a pessoa vinculada (RN019)")
        void atualizar_comNomeUsuarioValido_retornaStatus200() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));
            Pessoa outra = criarPessoa("Outra Pessoa", "99988877766", TipoPessoa.FISICA, "outra@teste.com");

            String corpo =
                    """
                    { "nomeUsuario": "maria.nova", "pessoaId": %d }
                    """
                            .formatted(outra.getId());

            mockMvc.perform(
                            put("/acessos/contas/{id}", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(corpo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nomeUsuario").value("maria.nova"))
                    .andExpect(jsonPath("$.pessoaId").value(pessoaFisica.getId()))
                    .andExpect(jsonPath("$.nomePessoa").value("Maria Silva"));

            Conta atualizada = contaRepository.findById(conta.getId()).orElseThrow();
            assertThat(atualizada.getPessoa().getId()).isEqualTo(pessoaFisica.getId());
        }

        @Test
        @DisplayName("Deve retornar 400 quando o nome de usuário estiver em branco")
        void atualizar_comNomeUsuarioEmBranco_retornaStatus400() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            put("/acessos/contas/{id}", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"nomeUsuario\": \"\" }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 409 quando o nome de usuário pertencer a outra conta")
        void atualizar_comNomeUsuarioDeOutraConta_retornaStatus409() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));
            Pessoa outra = criarPessoa("Outra Pessoa", "99988877766", TipoPessoa.FISICA, "outra@teste.com");
            criarConta(outra, "outra.pessoa", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            put("/acessos/contas/{id}", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"nomeUsuario\": \"outra.pessoa\" }"))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Deve permitir manter o próprio nome de usuário")
        void atualizar_comMesmoNomeUsuario_retornaStatus200() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            put("/acessos/contas/{id}", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"nomeUsuario\": \"maria.silva\" }"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 404 para conta inexistente")
        void atualizar_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(
                            put("/acessos/contas/{id}", 999999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"nomeUsuario\": \"qualquer\" }"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 404 para conta inativa")
        void atualizar_comContaInativa_retornaStatus404() throws Exception {
            Conta conta = criarContaInativa(pessoaFisica, "maria.silva");

            mockMvc.perform(
                            put("/acessos/contas/{id}", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"nomeUsuario\": \"maria.nova\" }"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /acessos/contas/{id}/perfis - Atualização de perfis")
    class AtualizarPerfis {

        @Test
        @DisplayName("Deve substituir os perfis da conta")
        void atualizarPerfis_comPerfisValidos_retornaStatus200() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            put("/acessos/contas/{id}/perfis", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"perfis\": [\"GERENCIADOR_DOACOES\", \"GERENCIADOR_RELATORIOS\"] }"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.perfis", hasSize(2)))
                    .andExpect(
                            jsonPath("$.perfis[*].codigo")
                                    .value(containsInAnyOrder("GERENCIADOR_DOACOES", "GERENCIADOR_RELATORIOS")));

            assertThat(contaRepository.findById(conta.getId()).orElseThrow().getPerfis())
                    .containsExactlyInAnyOrder(Perfil.GERENCIADOR_DOACOES, Perfil.GERENCIADOR_RELATORIOS);
        }

        @Test
        @DisplayName("Deve retornar 400 quando a lista de perfis estiver vazia")
        void atualizarPerfis_comListaVazia_retornaStatus400() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            put("/acessos/contas/{id}/perfis", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"perfis\": [] }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 404 para conta inativa")
        void atualizarPerfis_comContaInativa_retornaStatus404() throws Exception {
            Conta conta = criarContaInativa(pessoaFisica, "maria.silva");

            mockMvc.perform(
                            put("/acessos/contas/{id}/perfis", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"perfis\": [\"GERENCIADOR_DOACOES\"] }"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /acessos/contas/{id}/redefinir-senha - Redefinição administrativa (US-59)")
    class RedefinirSenhaAdmin {

        @Test
        @DisplayName("Deve atualizar o hash e marcar redefinirSenha = true")
        void redefinirSenha_comSenhaValida_retornaStatus204() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));
            conta.definirSenhaDefinitiva(passwordEncoder.encode(SENHA));
            contaRepository.saveAndFlush(conta);

            mockMvc.perform(
                            patch("/acessos/contas/{id}/redefinir-senha", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"novaSenhaTemporaria\": \"Temporaria@2026\" }"))
                    .andExpect(status().isNoContent());

            Conta atualizada = contaRepository.findById(conta.getId()).orElseThrow();
            assertThat(atualizada.isRedefinirSenha()).isTrue();
            assertThat(passwordEncoder.matches("Temporaria@2026", atualizada.getSenhaHash())).isTrue();
            assertThat(passwordEncoder.matches(SENHA, atualizada.getSenhaHash())).isFalse();
        }

        @Test
        @DisplayName("Deve retornar 400 quando a nova senha temporária exceder 72 bytes")
        void redefinirSenha_comSenhaAcimaDe72Bytes_retornaStatus400() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            patch("/acessos/contas/{id}/redefinir-senha", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"novaSenhaTemporaria\": \"%s\" }".formatted("a".repeat(73))))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.invalidFields.novaSenhaTemporaria")
                                    .value("A senha deve ter no máximo 72 bytes."));
        }

        @Test
        @DisplayName("Deve retornar 400 quando a nova senha temporária estiver em branco")
        void redefinirSenha_comSenhaEmBranco_retornaStatus400() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            patch("/acessos/contas/{id}/redefinir-senha", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"novaSenhaTemporaria\": \"\" }"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 404 para conta inativa")
        void redefinirSenha_comContaInativa_retornaStatus404() throws Exception {
            Conta conta = criarContaInativa(pessoaFisica, "maria.silva");

            mockMvc.perform(
                            patch("/acessos/contas/{id}/redefinir-senha", conta.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"novaSenhaTemporaria\": \"Temporaria@2026\" }"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /acessos/contas/minha-senha - Troca de senha pelo próprio usuário (US-46)")
    class AlterarMinhaSenha {

        private static final String CORPO_VALIDO =
                """
                { "senhaAtual": "Senha@123", "novaSenha": "Definitiva@2026" }
                """;

        @Test
        @DisplayName("Deve trocar a senha do usuário autenticado e marcar redefinirSenha = false")
        void alterarMinhaSenha_comSenhaAtualCorreta_retornaStatus204() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            patch("/acessos/contas/minha-senha")
                                    .with(user("maria.silva"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(CORPO_VALIDO))
                    .andExpect(status().isNoContent());

            Conta atualizada = contaRepository.findById(conta.getId()).orElseThrow();
            assertThat(atualizada.isRedefinirSenha()).isFalse();
            assertThat(passwordEncoder.matches("Definitiva@2026", atualizada.getSenhaHash())).isTrue();
        }

        @Test
        @DisplayName("Deve trocar a senha usando o token obtido em login real")
        void alterarMinhaSenha_comTokenDeLoginReal_retornaStatus204() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            MvcResult login =
                    mockMvcSemAutenticacao.perform(
                                    post("/auth/login")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    """
                                                    { "nomeUsuario": "maria.silva", "senha": "Senha@123" }
                                                    """))
                            .andExpect(status().isOk())
                            .andReturn();
            String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");

            mockMvcSemAutenticacao.perform(
                            patch("/acessos/contas/minha-senha")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(CORPO_VALIDO))
                    .andExpect(status().isNoContent());

            Conta atualizada = contaRepository.findById(conta.getId()).orElseThrow();
            assertThat(atualizada.isRedefinirSenha()).isFalse();
            assertThat(passwordEncoder.matches("Definitiva@2026", atualizada.getSenhaHash())).isTrue();
        }

        @Test
        @DisplayName("Deve retornar 401 quando não houver usuário autenticado")
        void alterarMinhaSenha_semUsuarioAutenticado_retornaStatus401() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvcSemAutenticacao.perform(
                            patch("/acessos/contas/minha-senha")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(CORPO_VALIDO))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.title").value("Não Autorizado"));

            assertThat(contaRepository.findById(conta.getId()).orElseThrow().isRedefinirSenha()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar 401 quando o token for inválido")
        void alterarMinhaSenha_comTokenInvalido_retornaStatus401() throws Exception {
            criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvcSemAutenticacao.perform(
                            patch("/acessos/contas/minha-senha")
                                    .header("Authorization", "Bearer token-invalido")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(CORPO_VALIDO))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Deve retornar 400 quando a senha atual estiver incorreta")
        void alterarMinhaSenha_comSenhaAtualIncorreta_retornaStatus400() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            patch("/acessos/contas/minha-senha")
                                    .with(user("maria.silva"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"senhaAtual\": \"errada\", \"novaSenha\": \"Definitiva@2026\" }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.invalidFields.senhaAtual")
                                    .value("A senha atual está incorreta."));

            assertThat(contaRepository.findById(conta.getId()).orElseThrow().isRedefinirSenha()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar 400 quando a nova senha exceder 72 bytes")
        void alterarMinhaSenha_comNovaSenhaAcimaDe72Bytes_retornaStatus400() throws Exception {
            criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            patch("/acessos/contas/minha-senha")
                                    .with(user("maria.silva"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{ \"senhaAtual\": \"Senha@123\", \"novaSenha\": \"%s\" }"
                                                    .formatted("a".repeat(73))))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.invalidFields.novaSenha")
                                    .value("A senha deve ter no máximo 72 bytes."));
        }

        @Test
        @DisplayName("Deve retornar 400 quando os campos estiverem em branco")
        void alterarMinhaSenha_comCamposEmBranco_retornaStatus400() throws Exception {
            criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(
                            patch("/acessos/contas/minha-senha")
                                    .with(user("maria.silva"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ \"senhaAtual\": \"\", \"novaSenha\": \"\" }"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /acessos/contas/{id}/desativar e /reativar - Ciclo de vida lógico")
    class DesativarEReativar {

        @Test
        @DisplayName("Deve desativar conta ativa")
        void desativar_comContaAtiva_retornaStatus204() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(patch("/acessos/contas/{id}/desativar", conta.getId()))
                    .andExpect(status().isNoContent());

            assertThat(contaRepository.findById(conta.getId()).orElseThrow().isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar 404 ao desativar conta já inativa")
        void desativar_comContaJaInativa_retornaStatus404() throws Exception {
            Conta conta = criarContaInativa(pessoaFisica, "maria.silva");

            mockMvc.perform(patch("/acessos/contas/{id}/desativar", conta.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve reativar conta inativa cuja pessoa está ativa (RN02)")
        void reativar_comContaInativa_retornaStatus204() throws Exception {
            Conta conta = criarContaInativa(pessoaFisica, "maria.silva");

            mockMvc.perform(patch("/acessos/contas/{id}/reativar", conta.getId()))
                    .andExpect(status().isNoContent());

            assertThat(contaRepository.findById(conta.getId()).orElseThrow().isAtivo()).isTrue();
        }

        @Test
        @DisplayName("Deve tratar como idempotente a reativação de conta já ativa")
        void reativar_comContaJaAtiva_retornaStatus204() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));

            mockMvc.perform(patch("/acessos/contas/{id}/reativar", conta.getId()))
                    .andExpect(status().isNoContent());

            assertThat(contaRepository.findById(conta.getId()).orElseThrow().getDataInativo()).isNull();
        }

        @Test
        @DisplayName("Deve retornar 400 ao reativar conta cuja pessoa está inativa (RN02)")
        void reativar_comPessoaInativa_retornaStatus400() throws Exception {
            Conta conta = criarContaInativa(pessoaFisica, "maria.silva");
            pessoaFisica.desativar();
            pessoaRepository.saveAndFlush(pessoaFisica);

            mockMvc.perform(patch("/acessos/contas/{id}/reativar", conta.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.invalidFields.pessoaId")
                                    .value("A pessoa vinculada está inativa."));
        }

        @Test
        @DisplayName("Deve retornar 404 ao reativar conta inexistente")
        void reativar_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(patch("/acessos/contas/{id}/reativar", 999999L))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /acessos/contas - Listagem paginada e filtros")
    class Listar {

        @BeforeEach
        void prepararContas() {
            criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_ACESSO, Perfil.GERENCIADOR_PESSOAS));

            Pessoa joao = criarPessoa("João Souza", "55566677788", TipoPessoa.FISICA, "joao@teste.com");
            Conta contaJoao = criarConta(joao, "joao.souza", Set.of(Perfil.GERENCIADOR_DOACOES));
            contaJoao.definirSenhaDefinitiva(passwordEncoder.encode(SENHA));
            contaRepository.saveAndFlush(contaJoao);

            Pessoa ana = criarPessoa("Ana Lima", "99988877766", TipoPessoa.FISICA, "ana@teste.com");
            criarContaInativa(ana, "ana.lima");
        }

        @Test
        @DisplayName("Deve listar apenas contas ativas por padrão, com paginação")
        void listar_semFiltros_retornaApenasAtivasPaginadas() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados", hasSize(1)))
                    .andExpect(jsonPath("$.tamanhoPagina").value(1))
                    .andExpect(jsonPath("$.totalElementos").value(2))
                    .andExpect(jsonPath("$.totalPaginas").value(2))
                    .andExpect(jsonPath("$.dados[0].nomeUsuario").value("joao.souza"));
        }

        @Test
        @DisplayName("Deve filtrar pelo nome da pessoa vinculada")
        void listar_comFiltroNomePessoa_retornaContaCorrespondente() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("nomePessoa", "maria"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nomePessoa").value("Maria Silva"));
        }

        @Test
        @DisplayName("Deve filtrar pelo nome de usuário")
        void listar_comFiltroNomeUsuario_retornaContaCorrespondente() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("nomeUsuario", "JOAO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nomeUsuario").value("joao.souza"));
        }

        @Test
        @DisplayName("Deve filtrar pelo e-mail da pessoa vinculada")
        void listar_comFiltroEmail_retornaContaCorrespondente() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("email", "maria@"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].email").value("maria@teste.com"));
        }

        @Test
        @DisplayName("Deve filtrar por perfis, sem duplicar contas com vários perfis")
        void listar_comFiltroPerfis_retornaContasSemDuplicidade() throws Exception {
            mockMvc.perform(
                            get("/acessos/contas")
                                    .param("perfis", "GERENCIADOR_ACESSO")
                                    .param("perfis", "GERENCIADOR_PESSOAS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados", hasSize(1)))
                    .andExpect(jsonPath("$.dados[0].nomeUsuario").value("maria.silva"));
        }

        @Test
        @DisplayName("Deve filtrar pela flag redefinirSenha")
        void listar_comFiltroRedefinirSenha_retornaContasCorrespondentes() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("redefinirSenha", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nomeUsuario").value("joao.souza"))
                    .andExpect(jsonPath("$.dados[0].redefinirSenha").value(false));
        }

        @Test
        @DisplayName("Deve filtrar pela data de criação")
        void listar_comFiltroDataCriacao_retornaContasDoDia() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("dataCriacao", hoje()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(2));

            mockMvc.perform(get("/acessos/contas").param("dataCriacao", ontem()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(0));
        }

        @Test
        @DisplayName("Deve listar apenas contas desativadas com apenasInativos=true")
        void listar_comApenasInativos_retornaSomenteDesativadas() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("apenasInativos", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1))
                    .andExpect(jsonPath("$.dados[0].nomeUsuario").value("ana.lima"))
                    .andExpect(jsonPath("$.dados[0].ativo").value(false));
        }

        @Test
        @DisplayName("Deve combinar apenasInativos com os demais filtros e com dataInativo")
        void listar_comApenasInativosEDataInativo_filtraSobreDesativadas() throws Exception {
            mockMvc.perform(
                            get("/acessos/contas")
                                    .param("apenasInativos", "true")
                                    .param("nomeUsuario", "ana")
                                    .param("dataInativo", hoje()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(1));

            mockMvc.perform(
                            get("/acessos/contas")
                                    .param("apenasInativos", "true")
                                    .param("dataInativo", ontem()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(0));

            mockMvc.perform(
                            get("/acessos/contas")
                                    .param("apenasInativos", "true")
                                    .param("nomeUsuario", "maria"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(0));
        }

        @Test
        @DisplayName("Deve ignorar dataInativo quando apenasInativos não estiver ativo")
        void listar_comDataInativoSemApenasInativos_ignoraFiltro() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("dataInativo", ontem()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElementos").value(2));
        }

        @Test
        @DisplayName("Deve aplicar a ordenação padrão quando o campo não estiver na whitelist")
        void listar_comOrdenacaoInvalida_aplicaOrdenacaoPadrao() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("sort", "senhaHash,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].nomeUsuario").value("joao.souza"))
                    .andExpect(jsonPath("$.dados[1].nomeUsuario").value("maria.silva"));
        }

        @Test
        @DisplayName("Deve ordenar por campo permitido da whitelist")
        void listar_comOrdenacaoValida_aplicaOrdenacao() throws Exception {
            mockMvc.perform(get("/acessos/contas").param("sort", "nomeUsuario,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dados[0].nomeUsuario").value("maria.silva"));
        }
    }

    @Nested
    @DisplayName("GET /acessos/contas/{id} - Detalhamento (US-54)")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar o detalhe de conta ativa")
        void buscarPorId_comContaAtiva_retornaStatus200() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_ACESSO));

            mockMvc.perform(get("/acessos/contas/{id}", conta.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(conta.getId()))
                    .andExpect(jsonPath("$.nomePessoa").value("Maria Silva"))
                    .andExpect(jsonPath("$.email").value("maria@teste.com"))
                    .andExpect(jsonPath("$.nomeUsuario").value("maria.silva"))
                    .andExpect(jsonPath("$.perfis[0].codigo").value("GERENCIADOR_ACESSO"))
                    .andExpect(jsonPath("$.perfis[0].descricao").value("Gerenciador de Acesso"))
                    .andExpect(jsonPath("$.redefinirSenha").value(true))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andExpect(jsonPath("$.dataCriacao").value(hoje()))
                    .andExpect(jsonPath("$.dataInativo").doesNotExist())
                    .andExpect(jsonPath("$.senhaHash").doesNotExist());
        }

        @Test
        @DisplayName("Deve retornar o detalhe de conta inativa, com a data de inativação")
        void buscarPorId_comContaInativa_retornaStatus200() throws Exception {
            Conta conta = criarContaInativa(pessoaFisica, "maria.silva");

            mockMvc.perform(get("/acessos/contas/{id}", conta.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ativo").value(false))
                    .andExpect(jsonPath("$.dataInativo").value(hoje()));
        }

        @Test
        @DisplayName("Deve retornar 404 para conta inexistente")
        void buscarPorId_comIdInexistente_retornaStatus404() throws Exception {
            mockMvc.perform(get("/acessos/contas/{id}", 999999L)).andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /acessos/contas/perfis - Opções de perfis (US-57)")
    class ListarPerfis {

        @Test
        @DisplayName("Deve retornar os cinco perfis com código e descrição")
        void listarPerfis_retornaStatus200ComTodosOsPerfis() throws Exception {
            mockMvc.perform(get("/acessos/contas/perfis"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0].codigo").value("GERENCIADOR_PESSOAS"))
                    .andExpect(jsonPath("$[0].descricao").value("Gerenciador de Pessoas"));
        }
    }

    @Nested
    @DisplayName("Exposição de dados sensíveis")
    class DadosSensiveis {

        private static final String SENHA_CADASTRO = "SenhaCadastro@2026";

        private void assertSemDadosSensiveis(MvcResult resultado, String... segredos) throws Exception {
            String corpo = resultado.getResponse().getContentAsString();
            assertThat(corpo).doesNotContain("senhaHash");
            for (String segredo : segredos) {
                assertThat(corpo).doesNotContain(segredo);
            }
        }

        @Test
        @DisplayName("Nenhuma resposta, de sucesso ou de erro, deve conter o hash ou a senha")
        void respostas_naoExpoemHashNemSenha() throws Exception {
            MvcResult cadastro =
                    mockMvc.perform(
                                    post("/acessos/contas")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    requisicaoCadastro(
                                                            pessoaFisica.getId(), "maria.silva", SENHA_CADASTRO)))
                            .andExpect(status().isCreated())
                            .andReturn();

            Conta conta = contaRepository.findByNomeUsuario("maria.silva").orElseThrow();
            String hash = conta.getSenhaHash();
            assertSemDadosSensiveis(cadastro, hash, SENHA_CADASTRO);

            assertSemDadosSensiveis(
                    mockMvc.perform(get("/acessos/contas")).andExpect(status().isOk()).andReturn(),
                    hash,
                    SENHA_CADASTRO);

            assertSemDadosSensiveis(
                    mockMvc.perform(get("/acessos/contas/{id}", conta.getId()))
                            .andExpect(status().isOk())
                            .andReturn(),
                    hash,
                    SENHA_CADASTRO);

            assertSemDadosSensiveis(
                    mockMvc.perform(
                                    put("/acessos/contas/{id}", conta.getId())
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content("{ \"nomeUsuario\": \"maria.silva\" }"))
                            .andExpect(status().isOk())
                            .andReturn(),
                    hash,
                    SENHA_CADASTRO);

            assertSemDadosSensiveis(
                    mockMvc.perform(
                                    put("/acessos/contas/{id}/perfis", conta.getId())
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content("{ \"perfis\": [\"GERENCIADOR_ACESSO\"] }"))
                            .andExpect(status().isOk())
                            .andReturn(),
                    hash,
                    SENHA_CADASTRO);

            // 400 de Bean Validation: senha válida, nome de usuário inválido
            String senhaValidacao = "SenhaValidacao@2026";
            assertSemDadosSensiveis(
                    mockMvc.perform(
                                    post("/acessos/contas")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(requisicaoCadastro(pessoaFisica.getId(), "", senhaValidacao)))
                            .andExpect(status().isBadRequest())
                            .andReturn(),
                    hash,
                    senhaValidacao);

            // 409: pessoa já possui conta
            String senhaConflito = "SenhaConflito@2026";
            assertSemDadosSensiveis(
                    mockMvc.perform(
                                    post("/acessos/contas")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    requisicaoCadastro(
                                                            pessoaFisica.getId(), "outro.usuario", senhaConflito)))
                            .andExpect(status().isConflict())
                            .andReturn(),
                    hash,
                    senhaConflito);

            // 400 de minha-senha com senha atual incorreta
            String novaSenha = "NovaSenha@2026";
            assertSemDadosSensiveis(
                    mockMvc.perform(
                                    patch("/acessos/contas/minha-senha")
                                            .with(user("maria.silva"))
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    "{ \"senhaAtual\": \"senha-errada\", \"novaSenha\": \"%s\" }"
                                                            .formatted(novaSenha)))
                            .andExpect(status().isBadRequest())
                            .andReturn(),
                    hash,
                    "senha-errada",
                    novaSenha);
        }

        @Test
        @DisplayName("Login real e troca de senha não devem expor hash nem senha")
        void loginEMinhaSenha_naoExpoemHashNemSenha() throws Exception {
            Conta conta = criarConta(pessoaFisica, "maria.silva", Set.of(Perfil.GERENCIADOR_PESSOAS));
            String hash = conta.getSenhaHash();

            MvcResult login =
                    mockMvcSemAutenticacao.perform(
                                    post("/auth/login")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    """
                                                    { "nomeUsuario": "maria.silva", "senha": "Senha@123" }
                                                    """))
                            .andExpect(status().isOk())
                            .andReturn();
            assertSemDadosSensiveis(login, hash, SENHA);
            String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");

            MvcResult troca =
                    mockMvcSemAutenticacao.perform(
                                    patch("/acessos/contas/minha-senha")
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    "{ \"senhaAtual\": \"Senha@123\", \"novaSenha\": \"Definitiva@2026\" }"))
                            .andExpect(status().isNoContent())
                            .andReturn();
            assertThat(troca.getResponse().getContentAsString()).isEmpty();
        }
    }
}
