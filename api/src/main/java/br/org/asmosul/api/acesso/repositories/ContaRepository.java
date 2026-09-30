package br.org.asmosul.api.acesso.repositories;

import br.org.asmosul.api.acesso.models.Conta;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContaRepository
        extends JpaRepository<Conta, Long>, JpaSpecificationExecutor<Conta> {

    Optional<Conta> findByNomeUsuario(String nomeUsuario);

    Optional<Conta> findByNomeUsuarioAndDataInativoIsNull(String nomeUsuario);

    @EntityGraph(attributePaths = {"pessoa"})
    Optional<Conta> findById(Long id);

    @EntityGraph(attributePaths = {"pessoa"})
    Optional<Conta> findByIdAndDataInativoIsNull(Long id);

    @EntityGraph(attributePaths = {"pessoa"})
    Page<Conta> findAll(Specification<Conta> spec, Pageable pageable);

    boolean existsByPessoaId(Long pessoaId);

    boolean existsByNomeUsuario(String nomeUsuario);

    boolean existsByNomeUsuarioAndIdNot(String nomeUsuario, Long id);
}
