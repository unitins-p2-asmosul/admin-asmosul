package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeInativavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "categoria_item")
public class CategoriaItem extends EntidadeInativavel {

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    protected CategoriaItem() {}

    public CategoriaItem(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void desativar() {
        this.setDataInativo(LocalDateTime.now());
    }

    public void reativar() {
        this.setDataInativo(null);
    }
}
