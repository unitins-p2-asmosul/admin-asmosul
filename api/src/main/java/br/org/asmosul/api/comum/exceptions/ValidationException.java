package br.org.asmosul.api.comum.exceptions;

import java.util.List;

public class ValidationException extends RuntimeException {

  public record CampoErro(String campo, String mensagem) {}

  private final List<CampoErro> erros;

  public ValidationException(String msg, List<CampoErro> erros) {
    super(msg);
    this.erros = (erros == null) ? List.of() : List.copyOf(erros);
  }

  public static ValidationException of(String campo, String mensagem) {
    return new ValidationException("Dados inválidos", List.of(new CampoErro(campo, mensagem)));
  }

  public List<CampoErro> getErros() {
    return erros;
  }
}
