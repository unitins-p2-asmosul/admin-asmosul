package br.org.asmosul.api.comum.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Testes Unitários - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve tratar ValidationException e preencher ProblemDetail com invalidFields")
    void deveTratarValidationException() {
        var erro1 = new ValidationException.CampoErro("nome", "O nome é obrigatório");
        var erro2 = new ValidationException.CampoErro("cpf", "O CPF deve conter 11 dígitos");
        var ex = new ValidationException("Dados inválidos fornecidos", List.of(erro1, erro2));

        ProblemDetail problemDetail = handler.tratarValidationException(ex);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Erro de Validação");
        assertThat(problemDetail.getDetail()).isEqualTo("Dados inválidos fornecidos");
        assertThat(problemDetail.getType())
                .isEqualTo(URI.create("https://api.asmosul.org.br/erros/validacao"));
        assertThat(problemDetail.getProperties()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, String> invalidFields =
                (Map<String, String>) problemDetail.getProperties().get("invalidFields");
        assertThat(invalidFields)
                .containsEntry("nome", "O nome é obrigatório")
                .containsEntry("cpf", "O CPF deve conter 11 dígitos");
    }

    @Test
    @DisplayName(
            "Deve tratar ConflitoDadosException e retornar ProblemDetail com status 409 Conflict")
    void deveTratarConflitoDadosException() {
        var ex = new ConflitoDadosException("Já existe um registro com este identificador.");

        ProblemDetail problemDetail = handler.tratarConflitoDadosException(ex);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Conflito de Dados");
        assertThat(problemDetail.getDetail())
                .isEqualTo("Já existe um registro com este identificador.");
        assertThat(problemDetail.getType())
                .isEqualTo(URI.create("https://api.asmosul.org.br/erros/conflito"));
    }

    @Test
    @DisplayName(
            "Deve tratar EntidadeNaoEncontradaException e retornar ProblemDetail com status 404 Not Found")
    void deveTratarEntidadeNaoEncontradaException() {
        var ex = new EntidadeNaoEncontradaException("Registro com o ID 42 não foi encontrado.");

        ProblemDetail problemDetail = handler.tratarEntidadeNaoEncontradaException(ex);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Recurso Não Encontrado");
        assertThat(problemDetail.getDetail()).isEqualTo("Registro com o ID 42 não foi encontrado.");
        assertThat(problemDetail.getType())
                .isEqualTo(URI.create("https://api.asmosul.org.br/erros/recurso-nao-encontrado"));
    }

    @Test
    @DisplayName(
            "Deve tratar Exception genérica e retornar ProblemDetail com status 500 Internal Server Error")
    void deveTratarErroAplicacaoGenerico() {
        var ex = new RuntimeException("Erro inesperado no banco de dados");

        ProblemDetail problemDetail = handler.tratarErroAplicacao(ex);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Erro Interno do Servidor");
        assertThat(problemDetail.getDetail())
                .isEqualTo(
                        "Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.");
        assertThat(problemDetail.getType())
                .isEqualTo(URI.create("https://api.asmosul.org.br/erros/erro-interno"));
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden quando usuário autenticado não tiver permissão")
    void deveTratarAccessDeniedComUsuarioAutenticadoComo403() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                "maria.silva",
                                null,
                                AuthorityUtils.createAuthorityList("ROLE_GERENCIADOR_PESSOAS")));

        ProblemDetail problemDetail =
                handler.tratarAcessoNegado(new AccessDeniedException("Access Denied"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Acesso Negado");
        assertThat(problemDetail.getType())
                .isEqualTo(URI.create("https://api.asmosul.org.br/erros/acesso-negado"));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando o usuário for anônimo")
    void deveTratarAccessDeniedComUsuarioAnonimoComo401() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new AnonymousAuthenticationToken(
                                "chave",
                                "anonymousUser",
                                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        ProblemDetail problemDetail =
                handler.tratarAcessoNegado(new AccessDeniedException("Access Denied"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Não Autorizado");
        assertThat(problemDetail.getType())
                .isEqualTo(URI.create("https://api.asmosul.org.br/erros/nao-autorizado"));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando não houver autenticação no contexto")
    void deveTratarCredenciaisAusentesComo401() {
        ProblemDetail problemDetail =
                handler.tratarAcessoNegado(
                        new AuthenticationCredentialsNotFoundException(
                                "An Authentication object was not found in the SecurityContext"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Não Autorizado");
    }
}
