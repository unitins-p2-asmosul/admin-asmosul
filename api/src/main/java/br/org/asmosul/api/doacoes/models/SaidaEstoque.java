package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "saida_estoque")
public class SaidaEstoque extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_saida", nullable = false)
    private Saida saida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_armazenamento", nullable = false)
    private EnderecoArmazenamento armazenamento;

    @Column(nullable = false)
    private Integer quantidade;

    protected SaidaEstoque() {}

    public SaidaEstoque(
        Item item,
        Saida saida,
        EnderecoArmazenamento armazenamento,
        Integer quantidade) {
        this.item = item;
        this.saida = saida;
        this.armazenamento = armazenamento;
        this.quantidade = quantidade;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Saida getSaida() {
        return saida;
    }

    public void setSaida(Saida saida) {
        this.saida = saida;
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
