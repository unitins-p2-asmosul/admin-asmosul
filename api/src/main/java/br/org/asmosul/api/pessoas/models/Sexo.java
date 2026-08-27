package br.org.asmosul.api.pessoas.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import br.org.asmosul.api.comum.exceptions.ValidationException;
import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Sexo {
    FEMININO("FEMININO", "Feminino"),
    MASCULINO("MASCULINO", "Masculino"),
    PREFIRO_NAO_INFORMAR("PREFIRO_NAO_INFORMAR", "Prefiro não informar");

    private final String codigo;
    private final String descricao;

    Sexo(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    @JsonProperty("codigo")
    public String getCodigo() { return codigo; }

    @JsonProperty("descricao")
    public String getDescricao() { return descricao; }

    @JsonCreator
    public static Sexo deCodigo(String valor) {
        if (valor == null || valor.isBlank()) return null;

        return Arrays.stream(Sexo.values())
                .filter(s -> s.getCodigo().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> ValidationException.of("sexo", "Opção de sexo informada é inválida"));
    }
}