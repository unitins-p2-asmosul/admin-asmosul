package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.models.Comorbidade;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComorbidadeRepository extends JpaRepository<Comorbidade, Long> {

    Optional<Comorbidade> findByIdAndDataInativoIsNull(Long id);

    Page<Comorbidade> findAllByDataInativoIsNull(Pageable pageable);

    List<Comorbidade> findAllByDataInativoIsNull();

    Page<Comorbidade> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Comorbidade> findByNomeContainingIgnoreCaseAndDataInativoIsNull(
            String nome, Pageable pageable);

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);

    @Query(
            "SELECT c.id, COUNT(DISTINCT p.id) "
                    + "FROM Pessoa p JOIN p.comorbidades c "
                    + "WHERE c.id IN :comorbidadeIds AND p.dataInativo IS NULL "
                    + "GROUP BY c.id")
    List<Object[]> contarPessoasAtivasPorComorbidadeIds(
            @Param("comorbidadeIds") Collection<Long> comorbidadeIds);
}
