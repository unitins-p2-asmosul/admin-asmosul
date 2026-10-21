package br.org.asmosul.api.doacoes.repositories.specifications;

import br.org.asmosul.api.doacoes.dtos.MovimentacaoDTO;
import br.org.asmosul.api.doacoes.models.Movimentacao;
import br.org.asmosul.api.doacoes.models.MovimentacaoEstoque;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class MovimentacaoSpecification {

    private MovimentacaoSpecification() {}

    public static Specification<Movimentacao> comFiltro(MovimentacaoDTO.MovimentacaoFiltroDTO filtro) {
        return (root, query, criteriaBuilder) -> {
            if (filtro == null) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filtro.localNovoId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("novoArmazenamento").get("id"), filtro.localNovoId()));
            }

            if (filtro.data() != null) {
                LocalDateTime inicioDoDia = filtro.data().atStartOfDay();
                LocalDateTime fimDoDia = filtro.data().atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("dataHora"), inicioDoDia, fimDoDia));
            }
            if (filtro.dataHoraInicio() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("dataHora"), filtro.dataHoraInicio()));
            }
            if (filtro.dataHoraFim() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("dataHora"), filtro.dataHoraFim()));
            }

            if (filtro.itemId() != null || filtro.localAntigoId() != null || filtro.quantidade() != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<MovimentacaoEstoque> subRoot = subquery.from(MovimentacaoEstoque.class);
                subquery.select(subRoot.get("movimentacao").get("id"));

                List<Predicate> subPredicates = new ArrayList<>();
                subPredicates.add(criteriaBuilder.equal(subRoot.get("movimentacao").get("id"), root.get("id")));

                if (filtro.itemId() != null) {
                    subPredicates.add(criteriaBuilder.equal(subRoot.get("item").get("id"), filtro.itemId()));
                }
                if (filtro.localAntigoId() != null) {
                    subPredicates.add(criteriaBuilder.equal(subRoot.get("armazenamentoAntigo").get("id"), filtro.localAntigoId()));
                }
                if (filtro.quantidade() != null) {
                    subPredicates.add(criteriaBuilder.equal(subRoot.get("quantidade"), filtro.quantidade()));
                }

                subquery.where(subPredicates.toArray(new Predicate[0]));
                predicates.add(criteriaBuilder.exists(subquery));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
