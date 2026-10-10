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
@Table(name = "saida")
public class Saida extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recebedor", nullable = false)
    private Pessoa recebedor;

    @Column(name = "data_hora", nullable = false)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String descricao;

    protected Saida() {}

    public Saida(Pessoa recebedor, LocalDateTime dataHora, String descricao) {
        this.recebedor = recebedor;
        this.dataHora = (dataHora != null) ? dataHora : LocalDateTime.now();
        this.descricao = descricao;
    }

    public Pessoa getRecebedor() {
        return recebedor;
    }

    public void setRecebedor(Pessoa recebedor) {
        this.recebedor = recebedor;
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
