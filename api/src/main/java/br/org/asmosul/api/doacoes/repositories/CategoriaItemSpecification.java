package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.CategoriaItem;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class CategoriaItemSpecification {

    private CategoriaItemSpecification() {}

    public static Specification<CategoriaItem> comFiltro(
            String nome, String descricao, boolean incluirInativos) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!incluirInativos) {
                predicates.add(criteriaBuilder.isNull(root.get("dataInativo")));
            }

            if (nome != null && !nome.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("nome")),
                                "%" + nome.trim().toLowerCase() + "%"));
            }

            if (descricao != null && !descricao.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("descricao")),
                                "%" + descricao.trim().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
