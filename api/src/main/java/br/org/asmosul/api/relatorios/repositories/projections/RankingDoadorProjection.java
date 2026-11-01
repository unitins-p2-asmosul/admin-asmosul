package br.org.asmosul.api.relatorios.repositories.projections;

public interface RankingDoadorProjection {
    Long getIdDoador();
    String getNomeDoador();
    Long getTotalDoacoesRealizadas();
    Long getTotalItensDoados();
}
