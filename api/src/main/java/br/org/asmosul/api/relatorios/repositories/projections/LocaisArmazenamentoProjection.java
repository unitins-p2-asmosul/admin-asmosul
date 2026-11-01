package br.org.asmosul.api.relatorios.repositories.projections;

public interface LocaisArmazenamentoProjection {
    Long getTotalLocais();
    Long getVolumeFisicoTotal();
    Long getLocaisEmUso();
}
