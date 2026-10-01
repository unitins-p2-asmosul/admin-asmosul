package br.org.asmosul.api.doacoes.models;

import br.org.asmosul.api.comum.models.EntidadeInativavel;
import br.org.asmosul.api.pessoas.models.Uf;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "endereco_doacao")
public class EnderecoDoacao extends EntidadeInativavel {

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false, length = 9)
    private String cep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Uf uf;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 50)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String logradouro;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(name = "informacoes_adicionais", columnDefinition = "TEXT")
    private String informacoesAdicionais;

    protected EnderecoDoacao() {}

    public EnderecoDoacao(
            String nome,
            String cep,
            Uf uf,
            String cidade,
            String bairro,
            String logradouro,
            String numero,
            String complemento,
            String informacoesAdicionais) {
        this.nome = nome;
        this.cep = cep;
        this.uf = uf;
        this.cidade = cidade;
        this.bairro = bairro;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.informacoesAdicionais = informacoesAdicionais;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public Uf getUf() {
        return uf;
    }

    public void setUf(Uf uf) {
        this.uf = uf;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getInformacoesAdicionais() {
        return informacoesAdicionais;
    }

    public void setInformacoesAdicionais(String informacoesAdicionais) {
        this.informacoesAdicionais = informacoesAdicionais;
    }

    public void desativar() {
        this.setDataInativo(LocalDateTime.now());
    }

    public void reativar() {
        this.setDataInativo(null);
    }
}
