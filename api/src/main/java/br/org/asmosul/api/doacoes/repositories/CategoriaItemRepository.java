package br.org.asmosul.api.doacoes.repositories;

import br.org.asmosul.api.doacoes.models.CategoriaItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoriaItemRepository
        extends JpaRepository<CategoriaItem, Long>, JpaSpecificationExecutor<CategoriaItem> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<CategoriaItem> findByIdAndDataInativoIsNull(Long id);

    Page<CategoriaItem> findAllByDataInativoIsNull(Pageable pageable);

    List<CategoriaItem> findAllByDataInativoIsNull();
}
