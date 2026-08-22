package br.org.asmosul.api.comum.exceptions;

public class EntidadeNaoEncontradaException extends RuntimeException {
    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}