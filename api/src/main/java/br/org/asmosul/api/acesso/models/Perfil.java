package br.org.asmosul.api.acesso.models;

import br.org.asmosul.api.comum.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Perfil {
    GERENCIADOR_PESSOAS("GERENCIADOR_PESSOAS", "Gerenciador de Pessoas"),
    GERENCIADOR_DOACOES("GERENCIADOR_DOACOES", "Gerenciador de Doações"),
    GERENCIADOR_CAPACITACOES("GERENCIADOR_CAPACITACOES", "Gerenciador de Capacitações"),
    GERENCIADOR_ACESSO("GERENCIADOR_ACESSO", "Gerenciador de Acesso"),
    GERENCIADOR_RELATORIOS("GERENCIADOR_RELATORIOS", "Gerenciador de Relatórios");

    private final String codigo;
    private final String descricao;

    Perfil(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    @JsonProperty("codigo")
    public String getCodigo() {
        return codigo;
    }

    @JsonProperty("descricao")
    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static Perfil deCodigo(String valor) {
        if (valor == null || valor.isBlank()) return null;

        return Arrays.stream(Perfil.values())
            .filter(
                e ->
                    e.name().equalsIgnoreCase(valor.trim())
                        || e.getCodigo().equalsIgnoreCase(valor.trim()))
            .findFirst()
            .orElseThrow(
                () ->
                    ValidationException.of(
                        "perfil",
                        "Opção de perfil informado é inválida"));
    }
}
