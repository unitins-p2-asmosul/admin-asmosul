package br.org.asmosul.api.relatorios.repositories.projections;

public interface GiroEstoqueProjection {
    Long getItemId();
    String getItemNome();
    String getCategoriaNome();
    String getUnidadeMedida();
    Integer getSaldoEstoque();
    Integer getTotalEntradas();
    Integer getTotalSaidas();
}
