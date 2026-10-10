package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque extends EntidadeBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_armazenamento_antigo", nullable = false)
    private EnderecoArmazenamento armazenamentoAntigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_movimentacao", nullable = false)
    private Movimentacao movimentacao;

    @Column(nullable = false)
    private Integer quantidade;

    protected MovimentacaoEstoque() {}

    public MovimentacaoEstoque(
        Item item,
        EnderecoArmazenamento armazenamentoAntigo,
        Movimentacao movimentacao,
        Integer quantidade) {
        this.item = item;
        this.armazenamentoAntigo = armazenamentoAntigo;
        this.movimentacao = movimentacao;
        this.quantidade = quantidade;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public EnderecoArmazenamento getArmazenamentoAntigo() {
        return armazenamentoAntigo;
    }

    public void setArmazenamentoAntigo(EnderecoArmazenamento armazenamentoAntigo) {
        this.armazenamentoAntigo = armazenamentoAntigo;
    }

    public Movimentacao getMovimentacao() {
        return movimentacao;
    }

    public void setMovimentacao(Movimentacao movimentacao) {
        this.movimentacao = movimentacao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
