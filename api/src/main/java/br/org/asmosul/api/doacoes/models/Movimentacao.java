package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeBase;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao")
public class Movimentacao extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_novo_armazenamento", nullable = false)
    private EnderecoArmazenamento novoArmazenamento;

    @Column(name = "data_hora", nullable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String descricao;

    protected Movimentacao() {}

    public Movimentacao(
        EnderecoArmazenamento novoArmazenamento,
        LocalDateTime dataHora,
        String descricao) {
        this.novoArmazenamento = novoArmazenamento;
        this.dataHora = (dataHora != null) ? dataHora : LocalDateTime.now();
        this.descricao = descricao;
    }

    public EnderecoArmazenamento getNovoArmazenamento() {
        return novoArmazenamento;
    }

    public void setNovoArmazenamento(EnderecoArmazenamento novoArmazenamento) {
        this.novoArmazenamento = novoArmazenamento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
