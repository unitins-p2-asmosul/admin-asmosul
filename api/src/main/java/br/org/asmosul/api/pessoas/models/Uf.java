package br.org.asmosul.api.pessoas.models;

import br.org.asmosul.api.comum.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Uf {
    AC("AC", "Acre"),
    AL("AL", "Alagoas"),
    AP("AP", "Amapá"),
    AM("AM", "Amazonas"),
    BA("BA", "Bahia"),
    CE("CE", "Ceará"),
    DF("DF", "Distrito Federal"),
    ES("ES", "Espírito Santo"),
    GO("GO", "Goiás"),
    MA("MA", "Maranhão"),
    MT("MT", "Mato Grosso"),
    MS("MS", "Mato Grosso do Sul"),
    MG("MG", "Minas Gerais"),
    PA("PA", "Pará"),
    PB("PB", "Paraíba"),
    PR("PR", "Paraná"),
    PE("PE", "Pernambuco"),
    PI("PI", "Piauí"),
    RJ("RJ", "Rio de Janeiro"),
    RN("RN", "Rio Grande do Norte"),
    RS("RS", "Rio Grande do Sul"),
    RO("RO", "Rondônia"),
    RR("RR", "Roraima"),
    SC("SC", "Santa Catarina"),
    SP("SP", "São Paulo"),
    SE("SE", "Sergipe"),
    TO("TO", "Tocantins");

    private final String codigo;
    private final String descricao;

    Uf(String codigo, String descricao) {
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
    public static Uf deCodigo(Object valor) {
        if (valor == null) {
            return null;
        }

        String texto = null;
        if (valor instanceof String s) {
            texto = s;
        } else if (valor instanceof java.util.Map<?, ?> map) {
            Object codigo = map.get("codigo");
            if (codigo != null) {
                texto = codigo.toString();
            } else {
                Object nome = map.get("name");
                if (nome != null) {
                    texto = nome.toString();
                }
            }
        }

        if (texto == null || texto.isBlank()) {
            return null;
        }

        final String finalTexto = texto.trim();
        return Arrays.stream(Uf.values())
                .filter(
                        u ->
                                u.name().equalsIgnoreCase(finalTexto)
                                        || u.getCodigo().equalsIgnoreCase(finalTexto))
                .findFirst()
                .orElseThrow(() -> ValidationException.of("uf", "UF informada é inválida"));
    }
}
