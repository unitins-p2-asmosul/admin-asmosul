package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.Kit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface KitRepository extends JpaRepository<Kit, Long>, JpaSpecificationExecutor<Kit> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Kit> findByIdAndDataInativoIsNull(Long id);

    List<Kit> findAllByDataInativoIsNull();
}
