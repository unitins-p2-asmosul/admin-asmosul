package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UnidadeMedida {
    QUILO("KG", "Quilo"),
    UNIDADE("UN", "Unidade"),
    GRAMA("G", "Grama"),
    METRO("M", "Metro"),
    CENTIMETRO("CM", "Centímetro"),
    MILIMETRO("MM", "Milímetro"),
    MILIGRAMA("MG", "Miligrama"),
    CAIXA("CX", "Caixa"),
    PACOTE("PCT", "Pacote"),
    FARDO("FD", "Fardo"),
    LATA("LT", "Lata");

    private final String codigo;
    private final String descricao;

    UnidadeMedida(String codigo, String descricao) {
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
    public static UnidadeMedida deCodigo(String valor) {
        if (valor == null || valor.isBlank()) return null;

        return Arrays.stream(UnidadeMedida.values())
            .filter(
                u ->
                    u.name().equalsIgnoreCase(valor.trim())
                        || u.getCodigo().equalsIgnoreCase(valor.trim())
                        || u.getDescricao().equalsIgnoreCase(valor.trim()))
            .findFirst()
            .orElseThrow(
                () ->
                    ValidationException.of(
                        "unidadeMedida",
                        "Opção de unidade de medida informada é inválida"));
    }
}
