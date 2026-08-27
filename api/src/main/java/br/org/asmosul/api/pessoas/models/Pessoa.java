package br.org.asmosul.api.pessoas.models;

import br.org.asmosul.api.comum.models.EntidadeInativavel;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pessoa")
public class Pessoa extends EntidadeInativavel {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @Column(nullable = false, length = 11)
    private String telefone;

    @Column(length = 50)
    private String email;

    @Enumerated(EnumType.STRING)
    private Escolaridade escolaridade;

    @Column(length = 50)
    private String profissao;

    @Enumerated(EnumType.STRING)
    @Column(name = "faixa_renda")
    private RendaFamiliar rendaFamiliar;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @ManyToMany
    @JoinTable(
        name = "pessoa_comorbidade",
        joinColumns = @JoinColumn(name = "id_pessoa"),
        inverseJoinColumns = @JoinColumn(name = "id_comorbidade")
    )
    private List<Comorbidade> comorbidades = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "pessoa_categoria",
        joinColumns = @JoinColumn(name = "id_pessoa"),
        inverseJoinColumns = @JoinColumn(name = "id_categoria")
    )
    private List<Categoria> categorias = new ArrayList<>();

    protected Pessoa() {}

    public Pessoa(String nome, String cpf, LocalDate dataNascimento, Sexo sexo, String telefone, 
                  String email, Escolaridade escolaridade, String profissao, RendaFamiliar rendaFamiliar, 
                  String descricao) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.telefone = telefone;
        this.email = email;
        this.escolaridade = escolaridade;
        this.profissao = profissao;
        this.rendaFamiliar = rendaFamiliar;
        this.descricao = descricao;
    }

    public void atualizarDados(String nome, String cpf, LocalDate dataNascimento, Sexo sexo, 
                               String telefone, String email, Escolaridade escolaridade, 
                               String profissao, RendaFamiliar rendaFamiliar, String descricao) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.telefone = telefone;
        this.email = email;
        this.escolaridade = escolaridade;
        this.profissao = profissao;
        this.rendaFamiliar = rendaFamiliar;
        this.descricao = descricao;
    }

    public void desativar() {
        this.setDataInativo(LocalDateTime.now());
    }

    public void reativar() {
        this.setDataInativo(null);
    }

    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public Sexo getSexo() { return sexo; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public Escolaridade getEscolaridade() { return escolaridade; }
    public String getProfissao() { return profissao; }
    public RendaFamiliar getRendaFamiliar() { return rendaFamiliar; }
    public String getDescricao() { return descricao; }
    public List<Comorbidade> getComorbidades() { return comorbidades; }
    public List<Categoria> getCategorias() { return categorias; }

    public void setComorbidades(List<Comorbidade> comorbidades) { this.comorbidades = comorbidades; }
    public void setCategorias(List<Categoria> categorias) { this.categorias = categorias; }
}