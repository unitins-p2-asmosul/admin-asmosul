package br.org.asmosul.api.acesso.models;

import br.org.asmosul.api.comum.models.EntidadeInativavel;
import br.org.asmosul.api.pessoas.models.Pessoa;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conta")
public class Conta extends EntidadeInativavel {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pessoa", nullable = false, unique = true)
    private Pessoa pessoa;

    @Column(name = "nome_usuario", nullable = false, unique = true, length = 100)
    private String nomeUsuario;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "conta_perfil", joinColumns = @JoinColumn(name = "id_conta"))
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 50)
    private Set<Perfil> perfis = new HashSet<>();

    protected Conta() {}

    public Conta(Pessoa pessoa, String nomeUsuario, String senhaHash, Set<Perfil> perfis) {
        this.pessoa = pessoa;
        this.nomeUsuario = nomeUsuario;
        this.senhaHash = senhaHash;
        this.perfis = perfis != null ? new HashSet<>(perfis) : new HashSet<>();
        this.dataCriacao = LocalDateTime.now();
    }

    @PrePersist
    void preencherDataCriacao() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }

    public void setPerfis(Set<Perfil> perfis) {
        this.perfis = perfis != null ? new HashSet<>(perfis) : new HashSet<>();
    }
}
