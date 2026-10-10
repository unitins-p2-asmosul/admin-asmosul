package br.org.asmosul.api.pessoas.dtos;

import java.util.List;

public record ItemFiltroDTO(
        String nome,
        List<Long> categorias,
        Double estoque,
        Double precoMinimo,
        Double precoMaximo,
        Boolean apenasInativos
) {}