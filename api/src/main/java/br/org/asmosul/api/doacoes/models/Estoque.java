package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "estoque",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_estoque_item_armazenamento",
            columnNames = {"id_item", "id_armazenamento"})
    })
public class Estoque extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_armazenamento", nullable = false)
    private EnderecoArmazenamento armazenamento;

    @Column(nullable = false)
    private Integer quantidade = 0;

    protected Estoque() {}

    public Estoque(Item item, EnderecoArmazenamento armazenamento, Integer quantidade) {
        this.item = item;
        this.armazenamento = armazenamento;
        this.quantidade = (quantidade != null) ? quantidade : 0;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public EnderecoArmazenamento getArmazenamento() {
        return armazenamento;
    }

    public void setArmazenamento(EnderecoArmazenamento armazenamento) {
        this.armazenamento = armazenamento;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
