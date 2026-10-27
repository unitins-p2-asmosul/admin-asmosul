package br.org.asmosul.api.doacoes.repositories.specifications;

import br.org.asmosul.api.doacoes.dtos.SaidaDTO;
import br.org.asmosul.api.doacoes.models.Saida;
import br.org.asmosul.api.doacoes.models.SaidaEstoque;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class SaidaSpecification {

    private SaidaSpecification() {}

    /** O filtro {@code kitsIds} ainda não é aplicado, pois a saída não registra kits. */
    public static Specification<Saida> comFiltro(SaidaDTO.SaidaFiltroDTO filtro) {
        return (root, query, cb) -> {
            if (filtro == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.recebedorId() != null) {
                predicates.add(cb.equal(root.get("recebedor").get("id"), filtro.recebedorId()));
            }

            if (filtro.data() != null) {
                LocalDateTime inicio = filtro.data().atStartOfDay();
                LocalDateTime fim = filtro.data().plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), inicio));
                predicates.add(cb.lessThan(root.get("dataHora"), fim));
            }

            boolean filtraItens =
                    (filtro.itensIds() != null && !filtro.itensIds().isEmpty())
                            || filtro.quantidade() != null;

            if (filtraItens) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<SaidaEstoque> saidaEstoque = subquery.from(SaidaEstoque.class);
                List<Predicate> itensPredicates = new ArrayList<>();

                itensPredicates.add(cb.equal(saidaEstoque.get("saida").get("id"), root.get("id")));

                if (filtro.itensIds() != null && !filtro.itensIds().isEmpty()) {
                    itensPredicates.add(saidaEstoque.get("item").get("id").in(filtro.itensIds()));
                }

                if (filtro.quantidade() != null) {
                    itensPredicates.add(
                            cb.equal(saidaEstoque.get("quantidade"), filtro.quantidade()));
                }

                subquery.select(saidaEstoque.get("saida").get("id"));
                subquery.where(itensPredicates.toArray(new Predicate[0]));
                predicates.add(root.get("id").in(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
