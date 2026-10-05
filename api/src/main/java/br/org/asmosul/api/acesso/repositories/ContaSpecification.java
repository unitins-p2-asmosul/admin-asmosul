package br.org.asmosul.api.acesso.repositories;

import br.org.asmosul.api.acesso.dtos.ContaFiltroDTO;
import br.org.asmosul.api.acesso.models.Conta;
import br.org.asmosul.api.acesso.models.Perfil;
import br.org.asmosul.api.pessoas.models.Pessoa;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ContaSpecification {

    private ContaSpecification() {}

    public static Specification<Conta> comFiltro(ContaFiltroDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            boolean apenasInativos = filtro != null && Boolean.TRUE.equals(filtro.apenasInativos());

            if (apenasInativos) {
                predicates.add(criteriaBuilder.isNotNull(root.get("dataInativo")));
            } else {
                predicates.add(criteriaBuilder.isNull(root.get("dataInativo")));
            }

            if (filtro == null) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }

            if (filtro.nomeUsuario() != null && !filtro.nomeUsuario().isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("nomeUsuario")),
                                "%" + filtro.nomeUsuario().trim().toLowerCase() + "%"));
            }

            boolean filtraNomePessoa = filtro.nomePessoa() != null && !filtro.nomePessoa().isBlank();
            boolean filtraEmail = filtro.email() != null && !filtro.email().isBlank();

            if (filtraNomePessoa || filtraEmail) {
                Join<Conta, Pessoa> joinPessoa = root.join("pessoa", JoinType.INNER);

                if (filtraNomePessoa) {
                    predicates.add(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(joinPessoa.get("nome")),
                                    "%" + filtro.nomePessoa().trim().toLowerCase() + "%"));
                }

                if (filtraEmail) {
                    predicates.add(
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(joinPessoa.get("email")),
                                    "%" + filtro.email().trim().toLowerCase() + "%"));
                }
            }

            if (filtro.perfis() != null && !filtro.perfis().isEmpty()) {
                Join<Conta, Perfil> joinPerfis = root.join("perfis", JoinType.INNER);
                predicates.add(joinPerfis.in(filtro.perfis()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            if (filtro.redefinirSenha() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("redefinirSenha"), filtro.redefinirSenha()));
            }

            if (filtro.dataCriacao() != null) {
                predicates.add(
                        noDia(criteriaBuilder, root.get("dataCriacao"), filtro.dataCriacao()));
            }

            // O filtro por data de inativação só se aplica à listagem de contas desativadas
            if (apenasInativos && filtro.dataInativo() != null) {
                predicates.add(
                        noDia(criteriaBuilder, root.get("dataInativo"), filtro.dataInativo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate noDia(
            CriteriaBuilder criteriaBuilder, Path<LocalDateTime> campo, LocalDate dia) {
        return criteriaBuilder.and(
                criteriaBuilder.greaterThanOrEqualTo(campo, dia.atStartOfDay()),
                criteriaBuilder.lessThan(campo, dia.plusDays(1).atStartOfDay()));
    }
}
