package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Movimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long>, JpaSpecificationExecutor<Movimentacao> {

    @Override
    @EntityGraph(attributePaths = {"novoArmazenamento"})
    Optional<Movimentacao> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"novoArmazenamento"})
    Page<Movimentacao> findAll(Specification<Movimentacao> spec, Pageable pageable);
}

