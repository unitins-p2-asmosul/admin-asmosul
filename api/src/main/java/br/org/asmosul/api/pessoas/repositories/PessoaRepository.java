package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.models.Pessoa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PessoaRepository
        extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {

    @EntityGraph(attributePaths = {"comorbidades", "categorias"})
    Optional<Pessoa> findByIdAndDataInativoIsNull(Long id);

    @EntityGraph(attributePaths = {"comorbidades", "categorias"})
    Page<Pessoa> findAllByDataInativoIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"comorbidades", "categorias"})
    Page<Pessoa> findAll(Pageable pageable);

    List<Pessoa> findAllByDataInativoIsNull();

    boolean existsByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
