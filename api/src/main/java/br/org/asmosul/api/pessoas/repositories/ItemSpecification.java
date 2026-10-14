package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.dtos.ItemFiltroDTO;
import br.org.asmosul.api.pessoas.models.Categoria;
import br.org.asmosul.api.pessoas.models.Item;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ItemSpecification {

    private ItemSpecification() {}

    public static Specification<Item> comFiltro(ItemFiltroDTO filtro, boolean incluirInativos) {
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

            if (filtro.categorias() != null && !filtro.categorias().isEmpty()) {
                Join<Item, Categoria> joinCategoria = root.join("categoria", JoinType.INNER);
                predicates.add(joinCategoria.get("id").in(filtro.categorias()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            if (filtro.precoMinimo() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("precoUnitario"), filtro.precoMinimo()));
            }

            if (filtro.precoMaximo() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("precoUnitario"), filtro.precoMaximo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}