package br.org.asmosul.api.doacoes.repositories.specifications;

import br.org.asmosul.api.doacoes.dtos.KitDTO;
import br.org.asmosul.api.doacoes.models.ItemKit;
import br.org.asmosul.api.doacoes.models.Kit;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class KitSpecification {

    private KitSpecification() {}

    public static Specification<Kit> comFiltro(KitDTO.KitFiltroDTO filtro) {
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

            if (filtro.descricao() != null && !filtro.descricao().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("descricao")),
                                "%" + filtro.descricao().trim().toLowerCase() + "%"));
            }

            if (filtro.itensIds() != null && !filtro.itensIds().isEmpty()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<ItemKit> itemKitRoot = subquery.from(ItemKit.class);
                subquery.select(itemKitRoot.get("kit").get("id"))
                        .where(
                                cb.equal(itemKitRoot.get("kit").get("id"), root.get("id")),
                                itemKitRoot.get("item").get("id").in(filtro.itensIds()));
                predicates.add(root.get("id").in(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
