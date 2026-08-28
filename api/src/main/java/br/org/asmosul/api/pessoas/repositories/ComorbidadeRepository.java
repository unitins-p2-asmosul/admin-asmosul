package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.models.Comorbidade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComorbidadeRepository extends JpaRepository<Comorbidade, Long> {

    Optional<Comorbidade> findByIdAndDataInativoIsNull(Long id);

    Page<Comorbidade> findAllByDataInativoIsNull(Pageable pageable);

    List<Comorbidade> findAllByDataInativoIsNull();

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
