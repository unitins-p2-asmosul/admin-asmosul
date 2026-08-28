package br.org.asmosul.api.pessoas.models;

import br.org.asmosul.api.comum.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RendaFamiliar {
    MENOS_DE_MIL("MENOS_DE_MIL", "Menos de mil"),
    MIL_DOIS("MIL_DOIS", "Entre mil e dois mil"),
    DOIS_TRES("DOIS_TRES", "Entre dois mil e três mil"),
    MAIS_DE_TRES("MAIS_DE_TRES", "Mais de três mil");

    private final String codigo;
    private final String descricao;

    RendaFamiliar(String codigo, String descricao) {
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
    public static RendaFamiliar deCodigo(String valor) {
        if (valor == null || valor.isBlank()) return null;

        return Arrays.stream(RendaFamiliar.values())
                .filter(
                        r ->
                                r.name().equalsIgnoreCase(valor.trim())
                                        || r.getCodigo().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(
                        () ->
                                ValidationException.of(
                                        "rendaFamiliar",
                                        "Opção de renda familiar informada é inválida"));
    }
}
