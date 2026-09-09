package br.org.asmosul.api.pessoas.dtos;

import br.org.asmosul.api.pessoas.models.Escolaridade;
import br.org.asmosul.api.pessoas.models.RendaFamiliar;
import br.org.asmosul.api.pessoas.models.Sexo;
import br.org.asmosul.api.pessoas.models.TipoPessoa;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record PessoaFiltroDTO(
        String nome,
        String cpfCnpj,
        TipoPessoa tipoPessoa,
        @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dataNascimento,
        Sexo sexo,
        String telefone,
        String email,
        Escolaridade escolaridade,
        String profissao,
        String bairro,
        RendaFamiliar rendaFamiliar,
        Long comorbidadeId,
        Long categoriaId,
        Integer quantidadeCoabitantes,
        Boolean ehBeneficiario,
        Boolean ehDoador,
        Boolean apenasInativos) {}
