package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Map;

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
    public static UnidadeMedida deCodigo(Object valor) {
        if (valor == null) {
            return null;
        }

        String texto = null;
        if (valor instanceof String s) {
            texto = s;
        } else if (valor instanceof Map<?, ?> map) {
            Object codigo = map.get("codigo");
            if (codigo != null) {
                texto = codigo.toString();
            } else {
                Object name = map.get("name");
                if (name != null) {
                    texto = name.toString();
                } else {
                    Object descricao = map.get("descricao");
                    if (descricao != null) {
                        texto = descricao.toString();
                    }
                }
            }
        }

        if (texto == null || texto.isBlank()) {
            return null;
        }

        final String finalTexto = texto.trim();
        return Arrays.stream(UnidadeMedida.values())
                .filter(
                        u ->
                                u.name().equalsIgnoreCase(finalTexto)
                                        || u.getCodigo().equalsIgnoreCase(finalTexto)
                                        || u.getDescricao().equalsIgnoreCase(finalTexto))
                .findFirst()
                .orElseThrow(
                        () ->
                                ValidationException.of(
                                        "unidadeMedida",
                                        "Opção de unidade de medida informada é inválida"));
    }
}
