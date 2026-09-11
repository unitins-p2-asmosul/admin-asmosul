package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.dtos.PessoaFiltroDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.models.Comorbidade;
import br.org.asmosul.api.pessoas.models.Pessoa;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class PessoaSpecification {

    private PessoaSpecification() {}

    public static Specification<Pessoa> comFiltro(PessoaFiltroDTO filtro, boolean incluirInativos) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro != null && Boolean.TRUE.equals(filtro.apenasInativos())) {
                predicates.add(criteriaBuilder.isNotNull(root.get("dataInativo")));
            } else if (!incluirInativos) {
                predicates.add(criteriaBuilder.isNull(root.get("dataInativo")));
            }

            if (filtro == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            if (filtro.nome() != null && !filtro.nome().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("nome")),
                                "%" + filtro.nome().trim().toLowerCase() + "%"));
            }

            if (filtro.cpfCnpj() != null && !filtro.cpfCnpj().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                root.get("cpfCnpj"), "%" + filtro.cpfCnpj().trim() + "%"));
            }

            if (filtro.tipoPessoa() != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipoPessoa"), filtro.tipoPessoa()));
            }

            if (filtro.dataNascimento() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("dataNascimento"), filtro.dataNascimento()));
            }

            if (filtro.sexo() != null) {
                predicates.add(criteriaBuilder.equal(root.get("sexo"), filtro.sexo()));
            }

            if (filtro.telefone() != null && !filtro.telefone().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                root.get("telefone"), "%" + filtro.telefone().trim() + "%"));
            }

            if (filtro.email() != null && !filtro.email().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("email")),
                                "%" + filtro.email().trim().toLowerCase() + "%"));
            }

            if (filtro.escolaridade() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("escolaridade"), filtro.escolaridade()));
            }

            if (filtro.profissao() != null && !filtro.profissao().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("profissao")),
                                "%" + filtro.profissao().trim().toLowerCase() + "%"));
            }

            if (filtro.bairro() != null && !filtro.bairro().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("bairro")),
                                "%" + filtro.bairro().trim().toLowerCase() + "%"));
            }

            if (filtro.rendaFamiliar() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("rendaFamiliar"), filtro.rendaFamiliar()));
            }

            if (filtro.quantidadeCoabitantes() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("quantidadeCoabitantes"), filtro.quantidadeCoabitantes()));
            }

            if (filtro.ehBeneficiario() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("ehBeneficiario"), filtro.ehBeneficiario()));
            }

            if (filtro.ehDoador() != null) {
                predicates.add(criteriaBuilder.equal(root.get("ehDoador"), filtro.ehDoador()));
            }

            if (filtro.comorbidadeId() != null) {
                Join<Pessoa, Comorbidade> joinComorbidade =
                        root.join("comorbidades", JoinType.INNER);
                predicates.add(
                        criteriaBuilder.equal(joinComorbidade.get("id"), filtro.comorbidadeId()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            if (filtro.categoriaId() != null) {
                Join<Pessoa, Categoria> joinCategoria = root.join("categorias", JoinType.INNER);
                predicates.add(
                        criteriaBuilder.equal(joinCategoria.get("id"), filtro.categoriaId()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
