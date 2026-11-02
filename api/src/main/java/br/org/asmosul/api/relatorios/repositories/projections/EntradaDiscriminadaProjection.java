package br.org.asmosul.api.relatorios.repositories.projections;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface EntradaDiscriminadaProjection {
    String getItemNome();
    String getCategoriaNome();
    Integer getQuantidade();
    BigDecimal getPrecoUnitario();
    BigDecimal getValorEstimado();
    String getEnderecoArmazenamentoNome();
    String getDoadorNome();
    String getCpfCnpj();
    LocalDateTime getDataHora();
    String getDescricao();
}
