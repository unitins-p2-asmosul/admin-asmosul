package br.org.asmosul.api.pessoas.repositories;

import br.org.asmosul.api.pessoas.models.Item;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Item> findByIdAndDataInativoIsNull(Long id);

    Page<Item> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT 0.0")
    Double calcularEstoqueAtual(@Param("itemId") Long itemId);
}