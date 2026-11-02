package br.org.asmosul.api.relatorios.repositories.projections;

public interface PessoaComorbidadeProjection {
    Long getIdPessoa();
    String getNome();
    String getCpfCnpj();
    String getTelefone();
    String getSexo();
    String getComorbidadesConcatenadas();
}
