package br.org.asmosul.api.pessoas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Escolaridade {
    FUNDAMENTAL_INCOMPLETO("FUNDAMENTAL_INCOMPLETO", "Fundamental Incompleto"),
    FUNDAMENTAL_COMPLETO("FUNDAMENTAL_COMPLETO", "Fundamental Completo"),
    ENSINO_MEDIO_INCOMPLETO("ENSINO_MEDIO_INCOMPLETO", "Ensino Médio Incompleto"),
    ENSINO_MEDIO_COMPLETO("ENSINO_MEDIO_COMPLETO", "Ensino Médio Completo"),
    SUPERIOR_INCOMPLETO("SUPERIOR_INCOMPLETO", "Superior Incompleto"),
    SUPERIOR_COMPLETO("SUPERIOR_COMPLETO", "Superior Completo");

    private final String codigo;
    private final String descricao;

    Escolaridade(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    @JsonProperty("codigo")
    public String getCodigo() { return codigo; }

    @JsonProperty("descricao")
    public String getDescricao() { return descricao; }

    @JsonCreator
    public static Escolaridade deCodigo(String valor) {
        if (valor == null || valor.isBlank()) return null;

        return Arrays.stream(Escolaridade.values())
                .filter(e -> e.getCodigo().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> ValidationException.of("escolaridade", "Opção de escolaridade informada é inválida"));
    }
}