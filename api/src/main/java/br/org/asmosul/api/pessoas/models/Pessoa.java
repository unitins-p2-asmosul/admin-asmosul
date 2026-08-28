package br.org.asmosul.api.pessoas.models;

import br.org.asmosul.api.comum.models.EntidadeInativavel;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "pessoa")
public class Pessoa extends EntidadeInativavel {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    @JsonFormat(pattern = "dd-MM-yyyy")
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
    private Set<Comorbidade> comorbidades = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "pessoa_categoria",
        joinColumns = @JoinColumn(name = "id_pessoa"),
        inverseJoinColumns = @JoinColumn(name = "id_categoria")
    )
    private Set<Categoria> categorias = new HashSet<>();

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

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Escolaridade getEscolaridade() { return escolaridade; }
    public void setEscolaridade(Escolaridade escolaridade) { this.escolaridade = escolaridade; }

    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }

    public RendaFamiliar getRendaFamiliar() { return rendaFamiliar; }
    public void setRendaFamiliar(RendaFamiliar rendaFamiliar) { this.rendaFamiliar = rendaFamiliar; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Set<Comorbidade> getComorbidades() { return comorbidades; }
    public void setComorbidades(Set<Comorbidade> comorbidades) { 
        this.comorbidades = (comorbidades != null) ? comorbidades : new HashSet<>(); 
    }

    public Set<Categoria> getCategorias() { return categorias; }
    public void setCategorias(Set<Categoria> categorias) { 
        this.categorias = (categorias != null) ? categorias : new HashSet<>(); 
    }
}