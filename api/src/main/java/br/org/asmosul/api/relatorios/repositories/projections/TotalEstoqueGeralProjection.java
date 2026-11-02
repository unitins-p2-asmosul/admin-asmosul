package br.org.asmosul.api.relatorios.repositories.projections;

import java.math.BigDecimal;

public interface TotalEstoqueGeralProjection {
    Long getSaldoFisicoGeral();
    BigDecimal getValorTotalEstimado();
}
