package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeBase;
import br.org.asmosul.api.pessoas.models.Pessoa;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrada")
public class Entrada extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doador", nullable = false)
    private Pessoa doador;

    @Column(name = "data_hora", nullable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String descricao;

    protected Entrada() {}

    public Entrada(Pessoa doador, LocalDateTime dataHora, String descricao) {
        this.doador = doador;
        this.dataHora = (dataHora != null) ? dataHora : LocalDateTime.now();
        this.descricao = descricao;
    }

    public Pessoa getDoador() {
        return doador;
    }

    public void setDoador(Pessoa doador) {
        this.doador = doador;
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
