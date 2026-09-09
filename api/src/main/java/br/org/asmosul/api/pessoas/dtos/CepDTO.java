package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.Uf;

public record CepDTO(
        String cep, String logradouro, String complemento, String bairro, String cidade, Uf uf) {}
