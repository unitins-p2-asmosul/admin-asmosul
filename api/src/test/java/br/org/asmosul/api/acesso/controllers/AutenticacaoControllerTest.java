package br.org.asmosul.api.acesso.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.asmosul.api.acesso.models.Conta;
import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.acesso.repositories.ContaRepository;
import br.org.asmosul.api.comum.config.BaseAPITest;
import br.org.asmosul.api.pessoas.models.Pessoa;
import br.org.asmosul.api.pessoas.models.TipoPessoa;
import br.org.asmosul.api.pessoas.repositories.PessoaRepository;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("Testes de integração - AutenticacaoController")
class AutenticacaoControllerTest extends BaseAPITest {

    private static final String SENHA = "Senha@123";

    @Autowired private MockMvc mockMvc;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private ContaRepository contaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararDados() {
        criarConta("usuario.ativo", "11122233344", false);
        criarConta("usuario.inativo", "55566677788", true);
    }

    @Test
    @DisplayName("Deve autenticar conta ativa e retornar JWT com payload estruturado")
    void login_comCredenciaisValidas_retorna200() throws Exception {
        String corpo = """
                {
                  "nomeUsuario": "usuario.ativo",
                  "senha": "Senha@123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiracao").isNotEmpty())
                .andExpect(jsonPath("$.nomeUsuario").value("usuario.ativo"))
                .andExpect(jsonPath("$.perfis[0].codigo").value("GERENCIADOR_PESSOAS"));
    }

    @Test
    @DisplayName("Deve rejeitar credenciais incorretas com 401 Unauthorized")
    void login_comSenhaIncorreta_retorna401() throws Exception {
        String corpo = """
                {
                  "nomeUsuario": "usuario.ativo",
                  "senha": "senha-incorreta"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(corpo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Deve rejeitar login de conta inativa com 401 Unauthorized")
    void login_comContaInativa_retorna401() throws Exception {
        String corpo = """
                {
                  "nomeUsuario": "usuario.inativo",
                  "senha": "Senha@123"
                }
                """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(corpo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    private void criarConta(String nomeUsuario, String cpf, boolean inativa) {
        Pessoa pessoa =
                new Pessoa(
                        "Pessoa " + nomeUsuario,
                        cpf,
                        TipoPessoa.FISICA,
                        null,
                        null,
                        "63999999999",
                        nomeUsuario + "@teste.com",
                        null,
                        null,
                        null,
                        "Pessoa criada para teste de autenticação",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0,
                        false,
                        false);
        pessoa = pessoaRepository.saveAndFlush(pessoa);

        Conta conta =
                new Conta(
                        pessoa,
                        nomeUsuario,
                        passwordEncoder.encode(SENHA),
                        Set.of(Perfil.GERENCIADOR_PESSOAS));
        if (inativa) {
            conta.setDataInativo(LocalDateTime.now());
        }
        contaRepository.saveAndFlush(conta);
    }
}
