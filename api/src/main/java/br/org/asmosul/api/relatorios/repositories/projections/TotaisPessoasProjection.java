package br.org.asmosul.api.relatorios.repositories.projections;

public interface TotaisPessoasProjection {
    Long getTotalAtivo();
    Long getTotalBeneficiario();
    Long getTotalDoador();
    Long getTotalInativo();
    Long getTotalCadastrosGerais();
}
