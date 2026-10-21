package br.org.asmosul.api.doacoes.repositories.specifications;

import br.org.asmosul.api.doacoes.dtos.ItemDTO;
import br.org.asmosul.api.doacoes.models.Estoque;
import br.org.asmosul.api.doacoes.models.Item;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ItemSpecification {

    private ItemSpecification() {}

    public static Specification<Item> comFiltro(ItemDTO.Filtro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro == null) {
                return cb.isNull(root.get("dataInativo"));
            }

            boolean apenasInativos = Boolean.TRUE.equals(filtro.apenasInativos());
            boolean incluirInativos = Boolean.TRUE.equals(filtro.incluirInativos());

            if (apenasInativos) {
                predicates.add(cb.isNotNull(root.get("dataInativo")));
            } else if (!incluirInativos) {
                predicates.add(cb.isNull(root.get("dataInativo")));
            }

            if (filtro.nome() != null && !filtro.nome().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("nome")),
                                "%" + filtro.nome().trim().toLowerCase() + "%"));
            }

            if (filtro.categoriasIds() != null && !filtro.categoriasIds().isEmpty()) {
                predicates.add(root.get("categoria").get("id").in(filtro.categoriasIds()));
            }

            if (filtro.precoUnitarioMinimo() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("precoUnitario"), filtro.precoUnitarioMinimo()));
            }

            if (filtro.precoUnitarioMaximo() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("precoUnitario"), filtro.precoUnitarioMaximo()));
            }

            if (filtro.estoque() != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Estoque> estoqueRoot = subquery.from(Estoque.class);
                subquery.select(estoqueRoot.get("item").get("id"))
                        .groupBy(estoqueRoot.get("item").get("id"))
                        .having(
                                cb.equal(
                                        cb.coalesce(cb.sum(estoqueRoot.get("quantidade")), 0),
                                        filtro.estoque()));

                if (filtro.estoque() == 0) {
                    Subquery<Long> subqueryItensComEstoque = query.subquery(Long.class);
                    Root<Estoque> subEstoque = subqueryItensComEstoque.from(Estoque.class);
                    subqueryItensComEstoque.select(subEstoque.get("item").get("id")).distinct(true);

                    predicates.add(
                            cb.or(
                                    root.get("id").in(subquery),
                                    cb.not(root.get("id").in(subqueryItensComEstoque))));
                } else {
                    predicates.add(root.get("id").in(subquery));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
