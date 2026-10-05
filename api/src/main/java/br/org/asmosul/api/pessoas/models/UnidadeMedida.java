package br.org.asmosul.api.pessoas.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Unidades de medida aceitas para itens")
public enum UnidadeMedida {
    KG("Quilo (kg)"),
    UN("Unidade (un / und)"),
    G("Grama (g)"),
    M("Metro (m)"),
    CM("Centímetro (cm)"),
    ML("Mililitro (ml)"),
    MG("Miligrama (mg)"),
    CX("Caixa (cx)"),
    PCT("Pacote (pct)"),
    FD("Fardo (fd)"),
    LT("Lata (lt)");

    private final String descricao;

    UnidadeMedida(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}