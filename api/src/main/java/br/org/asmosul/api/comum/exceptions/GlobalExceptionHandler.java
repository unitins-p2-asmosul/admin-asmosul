package br.org.asmosul.api.comum.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final AuthenticationTrustResolver TRUST_RESOLVER =
            new AuthenticationTrustResolverImpl();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logAviso("Requisição inválida", ex);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos");

        problemDetail.setTitle("Erro de Validação");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/validacao"));

        Map<String, String> errosMapeados =
                ex.getBindingResult().getFieldErrors().stream()
                        .collect(
                                Collectors.toMap(
                                        FieldError::getField,
                                        fieldError ->
                                                fieldError.getDefaultMessage() != null
                                                        ? fieldError.getDefaultMessage()
                                                        : "Inválido",
                                        (m1, m2) -> m1));

        problemDetail.setProperty("invalidFields", errosMapeados);

        return problemDetail;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail tratarHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        logAviso("Método HTTP não permitido", ex);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());

        problemDetail.setTitle("Método Não Permitido");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/metodo-nao-permitido"));

        return problemDetail;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail tratarValidationException(ValidationException ex) {
        logAviso("Erro de validação", ex);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());

        problemDetail.setTitle("Erro de Validação");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/validacao"));

        Map<String, String> errosMapeados =
                ex.getErros().stream()
                        .collect(
                                Collectors.toMap(
                                        ValidationException.CampoErro::campo,
                                        ValidationException.CampoErro::mensagem,
                                        (mensagemExistente, novaMensagem) -> mensagemExistente));

        problemDetail.setProperty("invalidFields", errosMapeados);

        return problemDetail;
    }

    @ExceptionHandler(ConflitoDadosException.class)
    public ProblemDetail tratarConflitoDadosException(ConflitoDadosException ex) {
        logAviso("Conflito de dados", ex);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());

        problemDetail.setTitle("Conflito de Dados");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/conflito"));

        return problemDetail;
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ProblemDetail tratarEntidadeNaoEncontradaException(EntidadeNaoEncontradaException ex) {
        logAviso("Recurso não encontrado", ex);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

        problemDetail.setTitle("Recurso Não Encontrado");
        problemDetail.setType(
                URI.create("https://api.asmosul.org.br/erros/recurso-nao-encontrado"));

        return problemDetail;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail tratarAuthenticationException(AuthenticationException ex) {
        logAviso("Falha de autenticação", ex);
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED, "Credenciais inválidas ou conta inativa.");

        problemDetail.setTitle("Não Autorizado");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/nao-autorizado"));

        return problemDetail;
    }

    // Requisições sem usuário autenticado recebem 401; usuários autenticados sem permissão, 403
    @ExceptionHandler({
        AccessDeniedException.class,
        AuthenticationCredentialsNotFoundException.class
    })
    public ProblemDetail tratarAcessoNegado(Exception ex) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || TRUST_RESOLVER.isAnonymous(autenticacao)) {
            logAviso("Acesso não autenticado", ex);
            ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(
                            HttpStatus.UNAUTHORIZED,
                            "Autenticação necessária para acessar este recurso.");
            problemDetail.setTitle("Não Autorizado");
            problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/nao-autorizado"));
            return problemDetail;
        }

        logAviso("Acesso negado", ex);

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN,
                        "Você não possui permissão para acessar este recurso.");
        problemDetail.setTitle("Acesso Negado");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/acesso-negado"));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroAplicacao(Exception ex) {
        HttpServletRequest request = obterRequisicaoAtual();
        if (request == null) {
            log.error("Erro interno sem contexto HTTP", ex);
        } else {
            log.error("Erro interno: {} {}", request.getMethod(), request.getRequestURI(), ex);
        }

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.");
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/erro-interno"));
        return problemDetail;
    }

    private void logAviso(String mensagem, Exception ex) {
        HttpServletRequest request = obterRequisicaoAtual();
        if (request == null) {
            log.warn("{} - {}", mensagem, ex.getMessage());
        } else {
            log.warn(
                    "{}: {} {} - {}",
                    mensagem,
                    request.getMethod(),
                    request.getRequestURI(),
                    ex.getMessage());
        }
    }

    private HttpServletRequest obterRequisicaoAtual() {
        if (RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes atributos) {
            return atributos.getRequest();
        }
        return null;
    }
}
