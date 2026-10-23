package br.org.asmosul.api.doacoes.repositories.specifications;

import br.org.asmosul.api.doacoes.dtos.EntradaDTO;
import br.org.asmosul.api.doacoes.models.Entrada;
import br.org.asmosul.api.doacoes.models.EntradaEstoque;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class EntradaSpecification {

    private EntradaSpecification() {}

    public static Specification<Entrada> comFiltro(EntradaDTO.EntradaFiltroDTO filtro) {
        return (root, query, cb) -> {
            if (filtro == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.doadorId() != null) {
                predicates.add(cb.equal(root.get("doador").get("id"), filtro.doadorId()));
            }

            if (filtro.data() != null) {
                LocalDateTime inicio = filtro.data().atStartOfDay();
                LocalDateTime fim = filtro.data().plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), inicio));
                predicates.add(cb.lessThan(root.get("dataHora"), fim));
            }

            boolean filtraItens =
                    filtro.itemId() != null
                            || filtro.quantidade() != null
                            || filtro.enderecoId() != null;

            if (filtraItens) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<EntradaEstoque> entradaEstoque = subquery.from(EntradaEstoque.class);
                List<Predicate> itensPredicates = new ArrayList<>();

                itensPredicates.add(
                        cb.equal(entradaEstoque.get("entrada").get("id"), root.get("id")));

                if (filtro.itemId() != null) {
                    itensPredicates.add(
                            cb.equal(entradaEstoque.get("item").get("id"), filtro.itemId()));
                }

                if (filtro.quantidade() != null) {
                    itensPredicates.add(
                            cb.equal(entradaEstoque.get("quantidade"), filtro.quantidade()));
                }

                if (filtro.enderecoId() != null) {
                    itensPredicates.add(
                            cb.equal(
                                    entradaEstoque.get("armazenamento").get("id"),
                                    filtro.enderecoId()));
                }

                subquery.select(entradaEstoque.get("entrada").get("id"));
                subquery.where(itensPredicates.toArray(new Predicate[0]));
                predicates.add(root.get("id").in(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
