package br.org.asmosul.api.relatorios.repositories.projections;

import java.math.BigDecimal;

public interface InventarioEstoqueProjection {
    Long getIdEstoque();
    Long getIdItem();
    String getItemNome();
    String getCategoriaNome();
    String getLocalArmazenamentoNome();
    Integer getQuantidade();
    String getUnidadeMedida();
    BigDecimal getValorUnitario();
    BigDecimal getValorTotal();
}
