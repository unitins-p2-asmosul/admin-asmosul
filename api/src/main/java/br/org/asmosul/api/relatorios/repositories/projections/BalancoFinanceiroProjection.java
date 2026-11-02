package br.org.asmosul.api.relatorios.repositories.projections;

import java.math.BigDecimal;

public interface BalancoFinanceiroProjection {
    BigDecimal getTotalSaidas();
    BigDecimal getTotalEntradas();
}
