package br.org.asmosul.api.comum.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class EntidadeInativavel extends EntidadeBase {

  @Column(name = "data_inativo")
  private LocalDateTime dataInativo;

  public LocalDateTime getDataInativo() {
    return dataInativo;
  }

  public void setDataInativo(LocalDateTime dataInativo) {
    this.dataInativo = dataInativo;
  }

  public boolean isAtivo() {
    return this.dataInativo == null;
  }
}
