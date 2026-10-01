package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.EnderecoDoacao;
import br.org.asmosul.api.pessoas.models.Uf;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class EnderecoDoacaoSpecification {

    private EnderecoDoacaoSpecification() {}

    public static Specification<EnderecoDoacao> comFiltro(
            String nome,
            String cep,
            Uf uf,
            String cidade,
            String bairro,
            String logradouro,
            boolean incluirInativos) {
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

            if (cep != null && !cep.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                root.get("cep"),
                                "%" + cep.trim().replaceAll("[^0-9]", "") + "%"));
            }

            if (uf != null) {
                predicates.add(criteriaBuilder.equal(root.get("uf"), uf));
            }

            if (cidade != null && !cidade.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("cidade")),
                                "%" + cidade.trim().toLowerCase() + "%"));
            }

            if (bairro != null && !bairro.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("bairro")),
                                "%" + bairro.trim().toLowerCase() + "%"));
            }

            if (logradouro != null && !logradouro.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("logradouro")),
                                "%" + logradouro.trim().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
