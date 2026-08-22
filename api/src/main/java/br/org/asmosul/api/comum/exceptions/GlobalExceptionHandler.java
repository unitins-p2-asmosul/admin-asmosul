package br.org.asmosul.api.comum.exceptions;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail tratarValidationException(ValidationException ex) {
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
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());

        problemDetail.setTitle("Conflito de Dados");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/conflito"));

        return problemDetail;
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ProblemDetail tratarEntidadeNaoEncontradaException(EntidadeNaoEncontradaException ex) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

        problemDetail.setTitle("Recurso Não Encontrado");
        problemDetail.setType(
                URI.create("https://api.asmosul.org.br/erros/recurso-nao-encontrado"));

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroAplicacao(Exception ex) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.");
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setType(URI.create("https://api.asmosul.org.br/erros/erro-interno"));
        return problemDetail;
    }
}
