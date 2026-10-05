package br.org.asmosul.api.acesso.dtos;

import br.org.asmosul.api.acesso.models.Perfil;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;

public record ContaFiltroDTO(
        String nomePessoa,
        String nomeUsuario,
        String email,
        Set<Perfil> perfis,
        Boolean redefinirSenha,
        @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dataCriacao,
        @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dataInativo,
        Boolean apenasInativos) {}
