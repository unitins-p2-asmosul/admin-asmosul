package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "entrada_estoque")
public class EntradaEstoque extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_armazenamento", nullable = false)
    private EnderecoArmazenamento armazenamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrada", nullable = false)
    private Entrada entrada;

    @Column(nullable = false)
    private Integer quantidade;

    protected EntradaEstoque() {}

    public EntradaEstoque(
        Item item,
        EnderecoArmazenamento armazenamento,
        Entrada entrada,
        Integer quantidade) {
        this.item = item;
        this.armazenamento = armazenamento;
        this.entrada = entrada;
        this.quantidade = quantidade;
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

    public Entrada getEntrada() {
        return entrada;
    }

    public void setEntrada(Entrada entrada) {
        this.entrada = entrada;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
