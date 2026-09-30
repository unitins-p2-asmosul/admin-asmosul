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
import org.hibernate.annotations.BatchSize;

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

    @Column(name = "redefinir_senha", nullable = false)
    private boolean redefinirSenha = true;

    @BatchSize(size = 25)
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
        this.redefinirSenha = true;
    }

    @PrePersist
    void preencherDataCriacao() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    // RN019 - A pessoa vinculada não pode ser alterada, apenas o nome de usuário
    public void alterarNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    // US-43 / US-59 - Senha temporária definida pelo Gerenciador de Acesso
    public void definirSenhaTemporaria(String senhaHash) {
        this.senhaHash = senhaHash;
        this.redefinirSenha = true;
    }

    // US-46 - Senha definitiva escolhida pelo próprio usuário
    public void definirSenhaDefinitiva(String senhaHash) {
        this.senhaHash = senhaHash;
        this.redefinirSenha = false;
    }

    public void atualizarPerfis(Set<Perfil> perfis) {
        this.perfis.clear();
        if (perfis != null) {
            this.perfis.addAll(perfis);
        }
    }

    public void desativar() {
        this.setDataInativo(LocalDateTime.now());
    }

    public void reativar() {
        this.setDataInativo(null);
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public boolean isRedefinirSenha() {
        return redefinirSenha;
    }

    public Set<Perfil> getPerfis() {
        return perfis;
    }
}
