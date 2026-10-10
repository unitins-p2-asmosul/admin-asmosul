package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeInativavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "item")
public class Item extends EntidadeInativavel {

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaItem categoria;

    @Column(name = "preco_unitario")
    private Integer precoUnitario;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false)
    private UnidadeMedida unidadeMedida;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    protected Item() {}

    public Item(
        String nome,
        CategoriaItem categoria,
        Integer precoUnitario,
        UnidadeMedida unidadeMedida,
        String descricao) {
        this.nome = nome;
        this.categoria = categoria;
        this.precoUnitario = precoUnitario;
        this.unidadeMedida = unidadeMedida;
        this.descricao = descricao;
    }

    public void desativar() {
        this.setDataInativo(LocalDateTime.now());
    }

    public void reativar() {
        this.setDataInativo(null);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CategoriaItem getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaItem categoria) {
        this.categoria = categoria;
    }

    public Integer getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(Integer precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeMedida unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
