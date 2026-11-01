package br.org.asmosul.api.relatorios.repositories.projections;

import java.math.BigDecimal;

public interface ItemMovimentacaoFinanceiraProjection {
    Long getItemId();
    String getItemNome();
    String getCategoriaNome();
    String getUnidadeMedida();
    Integer getQuantidadeTotal();
    BigDecimal getPrecoUnitarioMedio();
    BigDecimal getValorTotalFinanceiro();
}
