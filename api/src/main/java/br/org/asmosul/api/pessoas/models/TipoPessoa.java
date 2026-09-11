package br.org.asmosul.api.pessoas.models;

import br.org.asmosul.api.comum.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoPessoa {
    FISICA("FISICA", "Pessoa Física"),
    JURIDICA("JURIDICA", "Pessoa Jurídica");

    private final String codigo;
    private final String descricao;

    TipoPessoa(String codigo, String descricao) {
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
    public static TipoPessoa deCodigo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return Arrays.stream(TipoPessoa.values())
                .filter(
                        t ->
                                t.name().equalsIgnoreCase(valor.trim())
                                        || t.getCodigo().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(
                        () ->
                                ValidationException.of(
                                        "tipoPessoa",
                                        "Opção de tipo de pessoa informada é inválida"));
    }
}
