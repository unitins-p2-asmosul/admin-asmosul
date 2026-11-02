package br.org.asmosul.api.comum.dtos;

public record ItemRanking(
    int posicao,
    Long id,
    String nome,
    Long quantidade
) {}
