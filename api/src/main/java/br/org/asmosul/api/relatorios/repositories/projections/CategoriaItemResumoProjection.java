package br.org.asmosul.api.relatorios.repositories.projections;

public interface CategoriaItemResumoProjection {
    Long getIdCategoria();
    String getNome();
    String getDescricao();
    Long getItensDistintos();
    Long getSaldoFisico();
}
