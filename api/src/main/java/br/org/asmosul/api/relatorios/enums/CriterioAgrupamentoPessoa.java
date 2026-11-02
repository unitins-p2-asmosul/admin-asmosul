package br.org.asmosul.api.relatorios.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Critério dinâmico de agrupamento para o gráfico demográfico de pessoas")
public enum CriterioAgrupamentoPessoa {
    SEXO,
    ESCOLARIDADE,
    FAIXA_RENDA
}
