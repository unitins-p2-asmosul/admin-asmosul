package br.org.asmosul.api.pessoas.models;

import br.org.asmosul.api.comum.models.EntidadeInativavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "comorbidade")
public class Comorbidade extends EntidadeInativavel {

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    protected Comorbidade() {}

    public Comorbidade(String nome, String descricao) {
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
}
