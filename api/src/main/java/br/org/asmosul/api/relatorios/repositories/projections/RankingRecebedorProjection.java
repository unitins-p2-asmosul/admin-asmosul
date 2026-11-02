package br.org.asmosul.api.relatorios.repositories.projections;

public interface RankingRecebedorProjection {
    Long getIdRecebedor();
    String getNomeRecebedor();
    Integer getQuantidadeCoabitantes();
    Long getTotalEntregasRecebidas();
    Long getTotalItensRecebidos();
}
