package br.org.asmosul.api.doacoes.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.asmosul.api.comum.exceptions.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnidadeMedidaTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve deserializar UnidadeMedida a partir de diferentes formatos de texto")
    void deveDeserializarPorTexto() {
        assertThat(UnidadeMedida.deCodigo("QUILO")).isEqualTo(UnidadeMedida.QUILO);
        assertThat(UnidadeMedida.deCodigo("KG")).isEqualTo(UnidadeMedida.QUILO);
        assertThat(UnidadeMedida.deCodigo("kg")).isEqualTo(UnidadeMedida.QUILO);
        assertThat(UnidadeMedida.deCodigo("Quilo")).isEqualTo(UnidadeMedida.QUILO);
        assertThat(UnidadeMedida.deCodigo("Quilo (kg)")).isEqualTo(UnidadeMedida.QUILO);
        assertThat(UnidadeMedida.deCodigo("UNIDADE")).isEqualTo(UnidadeMedida.UNIDADE);
        assertThat(UnidadeMedida.deCodigo("UN")).isEqualTo(UnidadeMedida.UNIDADE);
        assertThat(UnidadeMedida.deCodigo("Unidade (un)")).isEqualTo(UnidadeMedida.UNIDADE);
    }

    @Test
    @DisplayName("Deve deserializar UnidadeMedida a partir de Map/JSON object")
    void deveDeserializarPorMap() {
        assertThat(UnidadeMedida.deCodigo(Map.of("codigo", "KG"))).isEqualTo(UnidadeMedida.QUILO);
        assertThat(UnidadeMedida.deCodigo(Map.of("sigla", "KG"))).isEqualTo(UnidadeMedida.QUILO);
        assertThat(UnidadeMedida.deCodigo(Map.of("nome", "QUILO"))).isEqualTo(UnidadeMedida.QUILO);
        assertThat(UnidadeMedida.deCodigo(Map.of("descricao", "Quilo (kg)"))).isEqualTo(UnidadeMedida.QUILO);
    }

    @Test
    @DisplayName("Deve lançar ValidationException para opção inválida")
    void deveLancarExceptionParaOpcaoInvalida() {
        assertThatThrownBy(() -> UnidadeMedida.deCodigo("INVALIDA"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Deve serializar com codigo, descricao, sigla e nome")
    void deveSerializarCorretamente() throws Exception {
        String json = objectMapper.writeValueAsString(UnidadeMedida.QUILO);
        Map<?, ?> map = objectMapper.readValue(json, Map.class);

        assertThat(map.get("codigo")).isEqualTo("KG");
        assertThat(map.get("descricao")).isEqualTo("Quilo");
        assertThat(map.get("sigla")).isEqualTo("KG");
        assertThat(map.get("nome")).isEqualTo("QUILO");
    }
}
