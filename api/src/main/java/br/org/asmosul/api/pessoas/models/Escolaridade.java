package br.org.asmosul.api.pessoas.models;

import br.org.asmosul.api.comum.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Escolaridade {
    FUNDAMENTAL_INCOMPLETO("FUNDAMENTAL_INCOMPLETO", "Fundamental Incompleto"),
    FUNDAMENTAL_COMPLETO("FUNDAMENTAL_COMPLETO", "Fundamental Completo"),
    MEDIO_INCOMPLETO("MEDIO_INCOMPLETO", "Ensino Médio Incompleto"),
    MEDIO_COMPLETO("MEDIO_COMPLETO", "Ensino Médio Completo"),
    SUPERIOR_INCOMPLETO("SUPERIOR_INCOMPLETO", "Superior Incompleto"),
    SUPERIOR_COMPLETO("SUPERIOR_COMPLETO", "Superior Completo");

    private final String codigo;
    private final String descricao;

    Escolaridade(String codigo, String descricao) {
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
    public static Escolaridade deCodigo(String valor) {
        if (valor == null || valor.isBlank()) return null;

        return Arrays.stream(Escolaridade.values())
                .filter(
                        e ->
                                e.name().equalsIgnoreCase(valor.trim())
                                        || e.getCodigo().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(
                        () ->
                                ValidationException.of(
                                        "escolaridade",
                                        "Opção de escolaridade informada é inválida"));
    }
}
