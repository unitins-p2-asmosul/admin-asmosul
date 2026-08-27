package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.models.Pessoa;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    @EntityGraph(attributePaths = {"comorbidades", "categorias"})
    Optional<Pessoa> findByIdAndDataInativoIsNull(Long id);

    @EntityGraph(attributePaths = {"comorbidades", "categorias"})
    Page<Pessoa> findAllByDataInativoIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"comorbidades", "categorias"})
    Page<Pessoa> findAll(Pageable pageable);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}